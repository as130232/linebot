package com.eachnow.linebot.domain.service.schedule.quartz.job;

import com.eachnow.linebot.common.constant.CommonConstant;
import com.eachnow.linebot.common.db.po.RemindPO;
import com.eachnow.linebot.common.db.repository.RemindRepository;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class RemindJob implements Job {
    private final RemindRepository remindRepository;
    private final LineNotifySender lineNotifySender;
    private final MessageSender messageSender;
    private final LineUserService lineUserService;
    private final LineConfig lineConfig;

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
        String label = "『提醒』 " + jobExecutionContext.getMergedJobDataMap().get("label").toString();
        Object userIdObj = jobExecutionContext.getMergedJobDataMap().get("userId");
        if (Objects.isNull(userIdObj)) {
            lineNotifySender.sendToCharles(label);
        } else {
            String userId = userIdObj.toString();
            String token = lineUserService.getNotifyToken(userId);
            if (Strings.isEmpty(token)) {
                return;
            }
            lineNotifySender.send(token, label);
        }
        Object remind = jobExecutionContext.getMergedJobDataMap().get("remindId");
        if (Objects.nonNull(remind) && NumberUtils.isNumber(remind.toString())) {
            Integer remindId = Integer.valueOf(remind.toString());
            updateValid(remindId);
        }
    }

    /**
     * 更新DB排程狀態
     *
     * @param remindId 排程ID
     */
    private void updateValid(Integer remindId) {
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
