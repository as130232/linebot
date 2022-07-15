package com.eachnow.linebot.domain.service.schedule.quartz;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.schedule.quartz.job.RemindJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

@Slf4j
@Service
public class QuartzService {
    //    private final String JOB_NAME = "quartz_job_name";
    private final String JOB_GROUP = "quartz_job_group";
    private final String TRIGGER_NAME = "quartz_trigger_name";
    private final String TRIGGER_GROUP = "quartz_trigger_group";
    private RemindRepository remindRepository;
    private Scheduler scheduler;

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
                this.addJob(po.getId(), po.getUserId(), po.getLabel(), po.getCron());
            });
        } catch (Exception e) {
            log.error("Quartz scheduler加載任務列表，失敗! error msg:{}", e.getMessage());
        }
    }

    public JobKey getJobKey(String remindId) {
        JobKey jobKey = new JobKey(remindId, JOB_GROUP);
        return jobKey;
    }

    public void addJob(Integer remindId, String userId, String label, String cron) {
        try {
            JobKey jobKey = getJobKey(remindId.toString());
            if (scheduler.checkExists(jobKey)) {
                return;
            }
            JobDetail jobDetail = JobBuilder.newJob(RemindJob.class)
                    .withIdentity(jobKey)  //任務ID
                    .usingJobData("remindId", remindId.toString())
                    .usingJobData("userId", userId)
                    .usingJobData("label", label)
                    .build();
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron).inTimeZone(TimeZone.getTimeZone(DateUtils.CST_ZONE_ID));
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("CRON_TRIGGER_" + remindId, TRIGGER_GROUP).withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Quartz scheduler add Job success. remindId:{}, label:{}, cron:{}, userId:{}", remindId, label, cron, userId);
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
}
