package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.po.femas.FemasDataPO;
import com.eachnow.linebot.common.po.femas.FemasRecordPO;
import com.eachnow.linebot.common.po.femas.FemasResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.FemasApiService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.schedule.quartz.QuartzService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.quartz.*;

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


    /**
     * 提醒上班打卡
     */
    public void remindPunchIn() {
        ZonedDateTime today = DateUtils.getCurrentDateTime();
        String searchStart = today.minusDays(3).format(DateUtils.yyyyMMddDash); //前三天
        String searchEnd = today.format(DateUtils.yyyyMMddDash);
        //取得當天打卡紀錄
        FemasRecordPO currentRecord = localCacheService.getRecord(searchEnd);
        if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getStartTime())) {
            FemasResultPO femasResultPO = femasApiService.getRecords(searchStart, searchEnd);
            FemasDataPO datePO = femasResultPO.getResponse().getDatas().get(0);
            if (datePO.getIs_holiday()) { //  若當天放假
                return;
            }
            // 還未有打卡記錄則提醒
            if (Strings.isEmpty(datePO.getFirst_in())) {
                lineNotifySender.sendToCharles("偵測到未打卡，請趕緊打卡。");
            }
        }
    }

    /**
     * 紀錄上班時間，並新增排程下班時提醒打卡
     */
    public void remindPunchOut() {
        ZonedDateTime today = DateUtils.getCurrentDateTime();
        String currentDate = today.format(DateUtils.yyyyMMddDash);
        JobKey jobKey = quartzService.getJobKey(getRemindId(currentDate));
        try {
            //檢查是否已經有當天下班提醒排程
            if (scheduler.checkExists(jobKey)) {
                return;
            }
        } catch (SchedulerException e) {
            log.error("checkExists failed! error mg:{}", e.getMessage());
        }
        String searchStart = today.minusDays(3).format(DateUtils.yyyyMMddDash); //前三天
        //取得當天紀錄
        FemasRecordPO currentRecord = localCacheService.getRecord(currentDate);
        if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getStartTime())) {
            FemasResultPO femasResultPO = femasApiService.getRecords(searchStart, currentDate);
            FemasDataPO datePO = femasResultPO.getResponse().getDatas().get(0);     //取得第一筆當天資訊
            if (datePO.getIs_holiday() || Strings.isEmpty(datePO.getFirst_in())) {  //  若當天放假 或 還未有打卡記錄
                log.info("remindPunchOut termination. isHoliday:{}, firstIn:{}", datePO.getIs_holiday(), datePO.getFirst_in());
                return;
            }
            String startDatetimeStr = currentDate + " " + datePO.getFirst_in();    //2023-04-24 08:52
            ZonedDateTime startDatetime = DateUtils.parseDate(startDatetimeStr, DateUtils.yyyyMMddHHmmDash);
            ZonedDateTime endDatetime = startDatetime.plusHours(9);
            String endDatetimeStr = endDatetime.format(DateUtils.yyyyMMddHHmmDash);
            FemasRecordPO po = FemasRecordPO.builder().date(currentDate).startTime(startDatetimeStr)
                    .endTime(endDatetimeStr).build();
            localCacheService.setRecord(currentDate, po);
            //新增下班提醒排程
            String cron = QuartzService.getCron(endDatetime.format(DateUtils.yyyyMMdd), endDatetime.format(DateUtils.hhmmss));
            quartzService.addRemindJob(getRemindId(currentDate), "system", "打卡下班囉！ " + endDatetimeStr, cron);
        }
    }

    public String getRemindId(String date) {
        return "PUNCH_" + date;
    }
}
