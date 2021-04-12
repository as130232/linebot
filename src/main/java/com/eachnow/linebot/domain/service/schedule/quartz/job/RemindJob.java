package com.eachnow.linebot.domain.service.schedule.quartz.job;

import com.eachnow.linebot.domain.service.line.LineNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class RemindJob implements Job {
    private LineNotifyService lineNotifyService;

    @Autowired
    public RemindJob(LineNotifyService lineNotifyService) {
        this.lineNotifyService = lineNotifyService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

    }
}
