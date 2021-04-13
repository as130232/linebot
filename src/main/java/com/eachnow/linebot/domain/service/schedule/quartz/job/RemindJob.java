package com.eachnow.linebot.domain.service.schedule.quartz.job;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.constant.LineNotifyConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.domain.service.line.LineNotifyService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
public class RemindJob implements Job {
    private RemindRepository remindRepository;
    private LineNotifyService lineNotifyService;
    private MessageSender messageSender;

    @Autowired
    public RemindJob(RemindRepository remindRepository,
                     LineNotifyService lineNotifyService,
                     MessageSender messageSender) {
        this.remindRepository = remindRepository;
        this.lineNotifyService = lineNotifyService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("trigger remind job. jobExecutionContext:{}", jobExecutionContext);
        String userId = jobExecutionContext.getMergedJobDataMap().get("userId").toString();
        String label = "[提醒]" + jobExecutionContext.getMergedJobDataMap().get("label").toString();
        Integer remindId = Integer.valueOf(jobExecutionContext.getMergedJobDataMap().get("remindId").toString());
        lineNotifyService.send(LineNotifyConstant.OWN, label);
//        messageSender.send(userId, "text", label);
        Optional<RemindPO> optional = remindRepository.findById(remindId);
        if (optional.isPresent()) {
            RemindPO remindPO = optional.get();
            if (CommonConstant.ONCE.equals(remindPO.getType())) {
                remindPO.setValid(CommonConstant.DONE);
                remindRepository.save(remindPO);
            }
        }
    }
}
