package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.po.femas.FemasPayResultPO;
import com.eachnow.linebot.common.po.femas.FemasPunchDataPO;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.po.femas.FemasPunchResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.FemasApiService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import com.eachnow.linebot.domain.service.schedule.quartz.QuartzService;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
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
    private final MessageSender messageSender;
    @Value("${femas.token}")
    private String FEMAS_TOKEN_CHARLES;

    @Autowired
    public FemasService(LocalCacheService localCacheService,
                        FemasApiService femasApiService,
                        QuartzService quartzService, Scheduler scheduler,
                        LineNotifySender lineNotifySender,
                        LineUserService lineUserService, MessageSender messageSender) {
        this.localCacheService = localCacheService;
        this.femasApiService = femasApiService;
        this.quartzService = quartzService;
        this.scheduler = scheduler;
        this.lineNotifySender = lineNotifySender;
        this.lineUserService = lineUserService;
        this.messageSender = messageSender;
    }

    @PostConstruct
    private void init() {
        getRecordAndSetRemind(null);
    }

    public FemasPunchRecordPO getPunchRecordAndSetCache(String userName, String femasToken, String searchStart, String searchEnd) {
        FemasPunchResultPO femasPunchResultPO = femasApiService.getPunchRecords(femasToken, searchStart, searchEnd);
        FemasPunchDataPO datePO = femasPunchResultPO.getResponse().getDatas().get(0);     //取得第一筆當天資訊
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
        if (localCacheService.isRecordExist(searchEnd, userName)) {
            localCacheService.setPunchRecord(searchEnd, userName, po);
            log.info("setPunchRecord local cache success. userName:{}, punchOut:{}", userName, punchOutStr);
        }
        return po;
    }

    /**
     * 取得該天、用戶打卡記錄，若緩存沒有則重新呼叫API
     */
    public FemasPunchRecordPO getPunchRecord(String userName, String femasToken, String currentDate) {
        ZonedDateTime dateTime = DateUtils.parseDate(currentDate, DateUtils.yyyyMMddDash);
        String searchStart = dateTime.minusDays(1).format(DateUtils.yyyyMMddDash); //前一天
        FemasPunchRecordPO currentRecord = localCacheService.getPunchRecord(currentDate, userName);
        if (Objects.isNull(currentRecord) || Objects.isNull(currentRecord.getPunchIn())) {
            currentRecord = getPunchRecordAndSetCache(userName, femasToken, searchStart, currentDate);
        }
        return currentRecord;
    }

    /**
     * 取得該天對應所有用戶的打卡記錄，並設置提醒
     */
    public Map<String, FemasPunchRecordPO> getRecordAndSetRemind(String date) {
        date = Objects.isNull(date) ? DateUtils.getCurrentDate() : date;
        ZonedDateTime today = DateUtils.parseDate(date, DateUtils.yyyyMMddDash);
        String searchStart = today.minusDays(1).format(DateUtils.yyyyMMddDash); //前一天
        String searchEnd = today.format(DateUtils.yyyyMMddDash);
        Map<String, FemasPunchRecordPO> data = new HashMap<>();
        List<LineUserPO> users = lineUserService.listUser();
        for (LineUserPO user : users) {
            String userName = user.getName();
            String femasToken = user.getFemasToken();
            if (isUserInValid(userName, femasToken, user.getIsValid())) {
                continue;
            }
            FemasPunchRecordPO femasPunchRecordPO = getPunchRecordAndSetCache(userName, femasToken, searchStart, searchEnd);
            //設置用戶下班提醒
            remindPunchOutByUser(user);
            data.put(userName, femasPunchRecordPO);
        }
        return data;
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
            FemasPunchResultPO femasPunchResultPO = femasApiService.getPunchRecords(FEMAS_TOKEN_CHARLES, searchStart, searchEnd);
            FemasPunchDataPO datePO = femasPunchResultPO.getResponse().getDatas().get(0);
            if (datePO.getIs_holiday()) { //  若當天放假
                return;
            }
            // 還未有打卡記錄則提醒
            if (Strings.isEmpty(datePO.getFirst_in())) {
                String message = "偵測到未打卡，剩十分鐘請趕緊打卡！";
//                lineNotifySender.sendToCharles(message);
                messageSender.pushTextToCharles(message);
            }
        }
        log.info("remindPunchIn success.");
    }

    /**
     * 紀錄上班時間，並新增排程下班時提醒打卡
     */
    public void remindPunchOut() {
        List<LineUserPO> users = lineUserService.listUser();
        for (LineUserPO user : users) {
            remindPunchOutByUser(user);
        }
    }


    public void remindPunchOutByUser(LineUserPO user) {
        try {
            ZonedDateTime today = DateUtils.getCurrentDateTime();
            String currentDate = today.format(DateUtils.yyyyMMddDash);
            String userName = user.getName();
            String femasToken = user.getFemasToken();
            String notifyToken = user.getNotifyToken();
            if (isUserInValid(userName, femasToken, user.getIsValid())) {
                return;
            }
            FemasPunchRecordPO po = this.getPunchRecord(userName, femasToken, currentDate);
            if (Objects.isNull(po)) {
                return;
            }
            JobKey jobKey = quartzService.getJobKey(getJobKeyStr(currentDate, userName));
            //檢查是否已經有當天下班提醒排程
            if (scheduler.checkExists(jobKey)) {
                return;
            }
            ZonedDateTime punchOut = DateUtils.parseDateTime(po.getPunchOut(), DateUtils.yyyyMMddHHmmDash);
            boolean isLate = isLate(punchOut);
            //檢查是否遲到，若是超過七點下班則為超過十點打卡，為遲到需要提醒忘刷卡
            if (isLate && !notifyToken.isEmpty()) {
                String sendMessage = "今日打卡時間為：" + po.getPunchOut() + "，需提交忘刷單或請假！";
//                lineNotifySender.send(notifyToken, sendMessage);
                Message message = new TextMessage(sendMessage);
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                messageSender.push(user.getId(), messages);
            }
            //減少五分鐘，讓下班前五分鐘就觸發提醒
            ZonedDateTime minus5Minutes = punchOut.minusMinutes(5);
            //新增下班提醒排程
            String cron = QuartzService.getCron(minus5Minutes.format(DateUtils.yyyyMMdd), minus5Minutes.format(DateUtils.hhmmss));
            String prefix = isLate ? "[Delay]" : "倒數五分鐘，準備打卡下班囉！";
            String label = prefix + " " + po.getPunchOut();
            quartzService.addRemindJob(jobKey, null, user.getId(), label, cron);
            log.info("set remindPunchOut success. userName:{}, punchIn: {}, punchOut: {}, cron: {}", userName, po.getPunchIn(), po.getPunchOut(), cron);
        } catch (Exception e) {
            String errorMsg = "set remindPunchOut failed! error msg:" + e.getMessage();
            log.error("{}", errorMsg);
//            lineNotifySender.sendToCharles(errorMsg);
            messageSender.pushTextToCharles(errorMsg);
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
        ZonedDateTime targetTime = ZonedDateTime.now().withZoneSameInstant(DateUtils.CST_ZONE_ID).withHour(19).withMinute(1).withSecond(0).withNano(0);
        // 判断下班時間是否小於目標時間
        return !punchOut.isBefore(targetTime);
    }

    /**
     * 檢查上個月是否有遲到需要忘刷卡
     */
    public void checkWorkLateLastMonth() {
        ZonedDateTime today = DateUtils.getCurrentDateTime();
        ZonedDateTime currentOrBeforeMonth = today;
        // 取得當前的日期，若是一號～五號，則取得上個月，若是五號到三十號則取得該月
        if (today.getDayOfMonth() <= 5) {
            currentOrBeforeMonth = today.minusMonths(1);
        }
        // API只支援二十天筆數的資料，所以要分兩個日期
        // 獲取該月的一號到二十號
        ZonedDateTime firstDayOfLastMonth = currentOrBeforeMonth.withDayOfMonth(1);
        ZonedDateTime twentyDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(20);
        ZonedDateTime twentyOneDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(21);
        List<LineUserPO> users = lineUserService.listUser();
        List<String> usernames = new ArrayList<>(users.size());
        for (LineUserPO user : users) {
            String userName = user.getName();
            String femasToken = user.getFemasToken();
            if (isUserInValid(userName, femasToken, user.getIsValid())) {
                continue;
            }

            //上個月一號到二十號
            FemasPunchResultPO firstDayToTwentyDay = femasApiService.getPunchRecords(femasToken,
                    firstDayOfLastMonth.format(DateUtils.yyyyMMddDash), twentyDayOfLastMonth.format(DateUtils.yyyyMMddDash));
            //上個月二十一號到今日
            FemasPunchResultPO twentyOneDayToToday = femasApiService.getPunchRecords(femasToken,
                    twentyOneDayOfLastMonth.format(DateUtils.yyyyMMddDash), today.format(DateUtils.yyyyMMddDash));
            List<FemasPunchDataPO> listData = new ArrayList<>();
            listData.addAll(firstDayToTwentyDay.getResponse().getDatas());
            listData.addAll(twentyOneDayToToday.getResponse().getDatas());
            for (FemasPunchDataPO data : listData) {
                if (data.getIs_holiday()) { // 若是假日/颱風假
                    continue;
                }
                if (data.getLeave_records().contains("特別休假") || data.getLeave_records().contains("全薪病假")) { //若是特休/全薪病假
                    continue;
                }
                String reason = "";
                //工時是否足夠(一日八小時)
                boolean isWorkingHoursSufficient = true;
                if (data.getRated_time() != null && data.getEff_att_time() != null) {
                    // 將字串轉換為 double
                    double ratedTime = Double.parseDouble(data.getRated_time());
                    double effAttTime = Double.parseDouble(data.getEff_att_time());
                    if (effAttTime < ratedTime && !DateUtils.getCurrentDate().equals(data.getAtt_date())) {
                        isWorkingHoursSufficient = false;
                        reason = "工時不足八小時";
                    }
                }
                boolean isLate = false;
                if (data.getLate_time() > 0) {
                    isLate = true;
                    reason = "遲到";
                }
                //若有遲到，或是工時不足八小時，則發送提醒
                if (isLate || !isWorkingHoursSufficient) {
                    String date = data.getAtt_date();
                    String inAndOut = data.getFirst_in() + " - " + data.getFirst_out();
                    String sendMessage = date + "，打卡時間：" + inAndOut + "，原因：" + reason + "，需提交忘刷單或請假！";
//                    lineNotifySender.send(user.getNotifyToken(), sendMessage);
                    Message message = new TextMessage(sendMessage);
                    List<Message> messages = new ArrayList<>();
                    messages.add(message);
                    messageSender.push(user.getId(), messages);
                }
            }
            usernames.add(user.getName());
        }
        log.info("checkWorkLateLastMonth. success. check users:{}", usernames);
    }

    public FemasPayResultPO getPayrollRecord(String femasToken, String yearMonth) {
        // 檢查日期格式是否符合 "yyyy/MM/dd"，則轉為yyyy-MM
        if (yearMonth.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
            try {
                // 解析日期字串
                LocalDate date = LocalDate.parse(yearMonth, DateUtils.yyyyMMddSlash);
                // 格式化為所需的格式
                yearMonth = date.format(DateUtils.yyyyMMDash);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format: " + e.getMessage());
            }
        }
        return femasApiService.getPayrollRecords(femasToken, yearMonth);
    }

    public boolean isUserInValid(String userName, String femasToken, Integer isValid) {
        if (Strings.isEmpty(userName) || Strings.isEmpty(femasToken) || CommonConstant.INVALID.equals(isValid)) {
            return true;
        }
        return false;
    }
}
