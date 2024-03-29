package com.eachnow.linebot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean(name = "ptt-crawler-executor")
    public ThreadPoolExecutor pttCrawlerExecutor(){
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,
                3,
                5,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100),
                new CustomizableThreadFactory("ptt-crawler-thread-"));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
