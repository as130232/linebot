package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.po.femas.FemasDataPO;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.po.femas.FemasResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.FemasApiService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.schedule.quartz.QuartzService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Component
public class FemasService {

    private final LocalCacheService localCacheService;
    private final FemasApiService femasApiService;
    private final QuartzService quartzService;
    private final Scheduler scheduler;
    private final LineNotifySender lineNotifySender;
    private final LineUserService lineUserService;
    @Value("${femas.token}")
    private String FEMAS_TOKEN_CHARLES;

    @Autowired
    public FemasService(LocalCacheService localCacheService,
                        FemasApiService femasApiService,
                        QuartzService quartzService, Scheduler scheduler,
                        LineNotifySender lineNotifySender,
                        LineUserService lineUserService) {
        this.localCacheService = localCacheService;
        this.femasApiService = femasApiService;
        this.quartzService = quartzService;
        this.scheduler = scheduler;
        this.lineNotifySender = lineNotifySender;
        this.lineUserService = lineUserService;
    }

    public FemasPunchRecordPO getPunchRecordByCharles(String searchStart, String searchEnd) {
        return getPunchRecord("charles", FEMAS_TOKEN_CHARLES, searchStart, searchEnd);
    }

    public FemasPunchRecordPO getPunchRecord(String userName, String femasToken, String searchStart, String searchEnd) {
        FemasResultPO femasResultPO = femasApiService.getRecords(femasToken, searchStart, searchEnd);
        FemasDataPO datePO = femasResultPO.getResponse().getDatas().get(0);     //取得第一筆當天資訊
        if (datePO.getIs_holiday() || Strings.isEmpty(datePO.getFirst_in())) {  //  若當天放假 或 還未有打卡記錄
            log.info("remindPunchOut termination. punch in not found. date:{}, isHoliday:{}, firstIn:{}", searchEnd, datePO.getIs_holiday(), datePO.getFirst_in());
            return null;
        }
        String punchInStr = searchEnd + " " + datePO.getFirst_in();       //2023-04-24 08:52
        String actualPunchOut = Objects.isNull(datePO.getFirst_out()) ? punchInStr : searchEnd + " " + datePO.getFirst_out();
        ZonedDateTime punchIn = DateUtils.parseDateTime(punchInStr, DateUtils.yyyyMMddHHmmDash);
        ZonedDateTime punchOut = punchIn.plusHours(9);
        String punchOutStr = punchOut.format(DateUtils.yyyyMMddHHmmDash);
        FemasPunchRecordPO po = FemasPunchRecordPO.builder().date(searchEnd).punchIn(punchInStr)
                .punchOut(punchOutStr).actualPunchOut(actualPunchOut).build();
        localCacheService.setPunchRecord(searchEnd, userName, po);
        log.info("setPunchRecord local cache success. userName:{}, punchOut:{}", userName, punchOutStr);
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
        FemasPunchRecordPO currentRecord = localCacheService.getPunchRecord(searchEnd, "charles");
        if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getPunchIn())) {
            FemasResultPO femasResultPO = femasApiService.getRecords(FEMAS_TOKEN_CHARLES, searchStart, searchEnd);
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
        List<LineUserPO> users = new ArrayList<>();
        try {
            users = lineUserService.listUser();
        } catch (Exception e) {
            users.add(LineUserPO.builder().name("charles").femasToken(FEMAS_TOKEN_CHARLES).build());
            log.error("set remindPunchOut and listUser failed! error mg:{}", e.getMessage());
            lineNotifySender.sendToCharles("set remindPunchOut and listUser failed!");
        }
        for (LineUserPO user : users) {
            remindPunchOutByUser(user);
        }
    }

    public void remindPunchOutByUser(LineUserPO user) {
        try {
            ZonedDateTime today = DateUtils.getCurrentDateTime();
            String currentDate = today.format(DateUtils.yyyyMMddDash);
            String searchStart = today.minusDays(2).format(DateUtils.yyyyMMddDash); //前三天
            String userName = user.getName().toLowerCase();
            String femasToken = user.getFemasToken();
            String notifyToken = user.getNotifyToken();
            if (Strings.isEmpty(userName) || Strings.isEmpty(femasToken)) {
                return;
            }
            JobKey jobKey = quartzService.getJobKey(getJobKeyStr(currentDate, userName));
            //檢查是否已經有當天下班提醒排程
            if (scheduler.checkExists(jobKey) || Objects.nonNull(localCacheService.getPunchRecord(currentDate, userName))) {
                return;
            }
            //取得當天紀錄
            FemasPunchRecordPO currentRecord = localCacheService.getPunchRecord(currentDate, userName);
            if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getPunchIn())) {
                FemasPunchRecordPO po = getPunchRecord(userName, femasToken, searchStart, currentDate);
                if (Objects.isNull(po)) {
                    return;
                }
                ZonedDateTime punchOut = DateUtils.parseDateTime(po.getPunchOut(), DateUtils.yyyyMMddHHmmDash);
                //檢查是否遲到，若是超過七點下班則為超過十點打卡，為遲到需要提醒忘刷卡
                if (isLate(punchOut) && !notifyToken.isEmpty()) {
                    lineNotifySender.send(notifyToken, "今日打卡時間為：" + po.getPunchOut() + "，需提交忘刷單或請假！");
                }
                //新增下班提醒排程
                String cron = QuartzService.getCron(punchOut.format(DateUtils.yyyyMMdd), punchOut.format(DateUtils.hhmmss));
                String label = "打卡下班囉！ " + po.getPunchOut();
                quartzService.addRemindJob(jobKey, null, null, label, cron);
                log.info("set remindPunchOut punchIn: {}, punchOut: {}, cron: {}", po.getPunchIn(), po.getPunchOut(), cron);
            }
            log.info("remindPunchOut success.");
        } catch (Exception e) {
            log.error("set remindPunchOut failed! error mg:{}", e.getMessage());
            lineNotifySender.sendToCharles("set remindPunchOut failed!");
        }
    }

    public String getJobKeyStr(String date, String userName) {
        return "PUNCH_" + (date + "_" + userName).toLowerCase();
    }

    /**
     * 檢查是否遲到，若是超過七點下班，為遲到需要提醒忘刷卡
     */
    public boolean isLate(ZonedDateTime punchOut) {
        //取得晚上7點整時間
        ZonedDateTime targetTime = ZonedDateTime.now().withZoneSameInstant(DateUtils.CST_ZONE_ID).withHour(19).withMinute(0).withSecond(0).withNano(0);
        // 判断下班時間是否小於目標時間
        return !punchOut.isBefore(targetTime);
    }
}
