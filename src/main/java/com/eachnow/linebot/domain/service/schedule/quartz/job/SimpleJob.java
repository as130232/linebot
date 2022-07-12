package com.eachnow.linebot.domain.service.schedule.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

@Slf4j
public class SimpleJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("job execute---" + new Date() + ", name:" + jobExecutionContext.getMergedJobDataMap().get("name") + "jobExecutionContext:" + jobExecutionContext);
    }
}
