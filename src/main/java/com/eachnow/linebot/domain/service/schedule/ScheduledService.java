package com.eachnow.linebot.domain.service.schedule;

import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 排程服務
 */
@Slf4j
@Component
public class ScheduledService {
    @Value("${cron.flag:false}")
    private boolean CRON_EXECUTE;
    private BeautyCrawlerService beautyCrawlerService;

    @Autowired
    public ScheduledService(BeautyCrawlerService beautyCrawlerService) {
        this.beautyCrawlerService = beautyCrawlerService;
    }

    public void switchCron(boolean isOpen) {
        CRON_EXECUTE = isOpen;
    }

    public boolean getCron() {
        return CRON_EXECUTE;
    }

    @Scheduled(cron = "${schedule.beauty.cron}")
    public void beautyCrawler() {
        if (!CRON_EXECUTE)
            return;
        log.info("[schedule]準備爬取表特版。time:{}", new Date());
        beautyCrawlerService.crawler(3);
        log.info("[schedule]爬取表特版，完成。time:{}", new Date());
    }

}
