package com.eachnow.linebot.domain.service.schedule.quartz.job;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
public class RemindJob implements Job {
    private RemindRepository remindRepository;
    private LineNotifySender lineNotifySender;
    private MessageSender messageSender;
    private LineUserService lineUserService;
    private LineConfig lineConfig;

    @Autowired
    public RemindJob(RemindRepository remindRepository,
                     LineNotifySender lineNotifySender,
                     MessageSender messageSender,
                     LineUserService lineUserService,
                     LineConfig lineConfig) {
        this.remindRepository = remindRepository;
        this.lineNotifySender = lineNotifySender;
        this.messageSender = messageSender;
        this.lineUserService = lineUserService;
        this.lineConfig = lineConfig;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("trigger remind job. jobExecutionContext:{}", jobExecutionContext);
        String userId = jobExecutionContext.getMergedJobDataMap().get("userId").toString();
        String label = "『提醒』 " + jobExecutionContext.getMergedJobDataMap().get("label").toString();
        Integer remindId = Integer.valueOf(jobExecutionContext.getMergedJobDataMap().get("remindId").toString());
        String token = lineUserService.getNotifyToken(userId);
        if (Strings.isEmpty(token)) {
            return;
        }
        lineNotifySender.sendToCharles(label);
//        messageSender.send(userId, "text", label);
        //更新DB排程狀態
//        Optional<RemindPO> optional = remindRepository.findById(remindId);
//        if (optional.isPresent()) {
//            RemindPO remindPO = optional.get();
//            if (CommonConstant.ONCE.equals(remindPO.getType())) {
//                remindPO.setValid(CommonConstant.DONE);
//                remindRepository.save(remindPO);
//            }
//        }
    }
}
