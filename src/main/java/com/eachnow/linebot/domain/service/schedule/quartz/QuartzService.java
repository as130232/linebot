package com.eachnow.linebot.domain.service.schedule.quartz;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.schedule.quartz.job.RemindJob;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

@Slf4j
@Service
public class QuartzService {
    //    private final String JOB_NAME = "quartz_job_name";
    private final String JOB_GROUP = "quartz_job_group";
    private final String TRIGGER_NAME = "quartz_trigger_name";
    private final String TRIGGER_GROUP = "quartz_trigger_group";
    private final RemindRepository remindRepository;
    private final Scheduler scheduler;

    @Autowired
    public QuartzService(Scheduler scheduler,
                         RemindRepository remindRepository) {
        this.scheduler = scheduler;
        this.remindRepository = remindRepository;
    }

    @PostConstruct
    public void init() {
        try {
            scheduler.start();
            log.info("Scheduler start success.");
            //取得資料庫中所有有效的提醒任務
            List<RemindPO> listRemind = remindRepository.findByValid(CommonConstant.VALID);
            listRemind.forEach(po -> {
                this.addRemindJob(getJobKey(String.valueOf(po.getId())), po.getId(), po.getUserId(), po.getLabel(), po.getCron());
            });
        } catch (Exception e) {
            log.error("Quartz scheduler加載任務列表，失敗! error msg:{}", e.getMessage());
        }
    }

    public JobKey getJobKey(String key) {
        return new JobKey(key, JOB_GROUP);
    }

    /**
     * 新增line通知提醒排程
     */
    public void addRemindJob(JobKey jobKey, Integer remindId, String userId, String label, String cron) {
        try {
            if (scheduler.checkExists(jobKey)) {
                return;
            }
            JobDetail jobDetail = JobBuilder.newJob(RemindJob.class)
                    .withIdentity(jobKey)  //任務ID
                    .usingJobData("remindId", remindId) //db remind id
                    .usingJobData("userId", userId)
                    .usingJobData("label", label)
                    .build();
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron).inTimeZone(TimeZone.getTimeZone(DateUtils.CST_ZONE_ID));
            String remindKey = UUID.randomUUID().toString().substring(0, 8);   // 八位數隨機值
            if (remindId != null) {
                remindKey = remindId.toString();
            }
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("CRON_TRIGGER_" + remindKey, TRIGGER_GROUP).withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Quartz scheduler add Job success. remindKey:{}, label:{}, cron:{}, userId:{}", remindKey, label, cron, userId);
        } catch (Exception e) {
            log.error("addJob failed! error msg:{}", e.getMessage());
        }
    }

    public void removeJob(String remindId) {
        try {
            JobKey jobKey = getJobKey(remindId);
            TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_NAME, TRIGGER_GROUP);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (Objects.isNull(trigger))
                return;
            // 停止觸發器
//            scheduler.pauseTrigger(triggerKey);
            // 移除觸發器
//            scheduler.unscheduleJob(triggerKey);
            // 刪除任務
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            log.error("removeJob failed! error msg:{}", e.getMessage());
        }
    }

    /**
     * 解析成cron格式
     *
     * @param date 20230424
     * @param time 09000000
     * @return 0 0 9 24 4 ? 2023
     */
    public static String getCron(String date, String time) {
        if (Strings.isEmpty(date) || Strings.isEmpty(time))
            return null;
        try {
            //20210408 161800 -> 0 18 16 8 4 ? 2021
            String cron = "{second} {minute} {hour} {day} {month} ? {year}";
            String cronOfYear = parseCronParam(date.substring(0, 4));
            String cronOfMonth = parseCronParam(date.substring(4, 6));
            String cronOfDay = parseCronParam(date.substring(6, 8));
            String cronOfHour = parseCronParam(time.substring(0, 2));
            String cronOfMinute = parseCronParam(time.substring(2, 4));
            String cronOfSecond = parseCronParam(time.substring(4, 6));
            return cron.replace("{year}", cronOfYear).replace("{month}", cronOfMonth).replace("{day}", cronOfDay)
                    .replace("{hour}", cronOfHour).replace("{minute}", cronOfMinute).replace("{second}", cronOfSecond);
        } catch (Exception e) {
            log.error("getCron failed! date:{}, time:{}, error msg:{}", date, time, e.getMessage());
        }
        return null;
    }

    /**
     * 解析cron 每一個時間參數 > 0 0 8 15 * ? *
     */
    private static String parseCronParam(String cron) {
        if (cron.contains("$") || cron.contains("＄")) {
            return "*";
        }
        if (cron.indexOf("0") == 0) {
            cron = cron.substring(1);
        }
        return cron;
    }
}
