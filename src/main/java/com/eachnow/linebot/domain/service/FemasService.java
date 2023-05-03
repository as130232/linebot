package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.po.femas.FemasDataPO;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.po.femas.FemasResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.FemasApiService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.schedule.quartz.QuartzService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Objects;

@Slf4j
@Component
public class FemasService {

    private final LocalCacheService localCacheService;
    private final FemasApiService femasApiService;
    private final QuartzService quartzService;
    private final Scheduler scheduler;
    private final LineNotifySender lineNotifySender;

    @Autowired
    public FemasService(LocalCacheService localCacheService,
                        FemasApiService femasApiService,
                        QuartzService quartzService, Scheduler scheduler,
                        LineNotifySender lineNotifySender) {
        this.localCacheService = localCacheService;
        this.femasApiService = femasApiService;
        this.quartzService = quartzService;
        this.scheduler = scheduler;
        this.lineNotifySender = lineNotifySender;
    }

    public FemasPunchRecordPO getPunchRecord(String searchStart, String searchEnd) {
        FemasResultPO femasResultPO = femasApiService.getRecords(searchStart, searchEnd);
        FemasDataPO datePO = femasResultPO.getResponse().getDatas().get(0);     //取得第一筆當天資訊
        if (datePO.getIs_holiday() || Strings.isEmpty(datePO.getFirst_in())) {  //  若當天放假 或 還未有打卡記錄
            log.info("remindPunchOut termination. date:{}, isHoliday:{}, firstIn:{}", searchEnd, datePO.getIs_holiday(), datePO.getFirst_in());
            return null;
        }
        String punchInStr = searchEnd + " " + datePO.getFirst_in();       //2023-04-24 08:52
        String actualPunchOut = Objects.isNull(datePO.getFirst_out()) ? punchInStr : searchEnd + " " + datePO.getFirst_out();
        ZonedDateTime punchIn = DateUtils.parseDateTime(punchInStr, DateUtils.yyyyMMddHHmmDash);
        ZonedDateTime punchOut = punchIn.plusHours(9);
        String punchOutStr = punchOut.format(DateUtils.yyyyMMddHHmmDash);
        FemasPunchRecordPO po = FemasPunchRecordPO.builder().date(searchEnd).punchIn(punchInStr)
                .punchOut(punchOutStr).actualPunchOut(actualPunchOut).build();
        localCacheService.setPunchRecord(searchEnd, po);
        return po;
    }

    /**
     * 提醒上班打卡
     */
    public void remindPunchIn() {
        ZonedDateTime today = DateUtils.getCurrentDateTime();
        String searchStart = today.minusDays(3).format(DateUtils.yyyyMMddDash); //前三天
        String searchEnd = today.format(DateUtils.yyyyMMddDash);
        //取得當天打卡紀錄
        FemasPunchRecordPO currentRecord = localCacheService.getPunchRecord(searchEnd);
        if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getPunchIn())) {
            FemasResultPO femasResultPO = femasApiService.getRecords(searchStart, searchEnd);
            FemasDataPO datePO = femasResultPO.getResponse().getDatas().get(0);
            if (datePO.getIs_holiday()) { //  若當天放假
                return;
            }
            // 還未有打卡記錄則提醒
            if (Strings.isEmpty(datePO.getFirst_in())) {
                lineNotifySender.sendToCharles("偵測到未打卡，剩十分鐘請趕緊打卡！");
            }
        }
        log.info("remindPunchIn success.");
    }

    /**
     * 紀錄上班時間，並新增排程下班時提醒打卡
     */
    public void remindPunchOut() {
        ZonedDateTime today = DateUtils.getCurrentDateTime();
        String currentDate = today.format(DateUtils.yyyyMMddDash);
        JobKey jobKey = quartzService.getJobKey(getJobKeyStr(currentDate));
        String searchStart = today.minusDays(3).format(DateUtils.yyyyMMddDash); //前三天
        try {
            //檢查是否已經有當天下班提醒排程
            if (scheduler.checkExists(jobKey) || Objects.nonNull(localCacheService.getPunchRecord(currentDate))) {
                return;
            }
        } catch (SchedulerException e) {
            log.error("checkExists failed! error mg:{}", e.getMessage());
        }
        //取得當天紀錄
        FemasPunchRecordPO currentRecord = localCacheService.getPunchRecord(currentDate);
        if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getPunchIn())) {
            FemasPunchRecordPO po = getPunchRecord(searchStart, currentDate);
            ZonedDateTime punchOut = DateUtils.parseDateTime(po.getPunchOut(), DateUtils.yyyyMMddHHmmDash);
            //新增下班提醒排程
            String cron = QuartzService.getCron(punchOut.format(DateUtils.yyyyMMdd), punchOut.format(DateUtils.hhmmss));
            log.info("set remindPunchOut punchIn: {}, punchOut: {}, cron: {}", po.getPunchIn(), po.getPunchOut(), cron);
            quartzService.addRemindJob(jobKey, null, null, "打卡下班囉！ " + po.getPunchOut(), cron);
        }
        log.info("remindPunchOut success.");
    }

    public String getJobKeyStr(String date) {
        return "PUNCH_" + date;
    }

}
