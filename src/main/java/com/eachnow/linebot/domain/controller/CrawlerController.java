package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.schedule.ScheduledService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {
    private ScheduledService scheduledService;
    private BeautyCrawlerService beautyCrawlerService;

    @Autowired
    public CrawlerController(ScheduledService scheduledService,
                             BeautyCrawlerService beautyCrawlerService) {
        this.scheduledService = scheduledService;
        this.beautyCrawlerService = beautyCrawlerService;
    }

    /**
     * 開/關閉所有排程
     */
    @GetMapping(value = "/cron/switch/{switchValue}")
    public Result switchCron(@PathVariable(name = "switchValue") boolean switchValue) {
        scheduledService.switchCron(switchValue);
        return Result.getDefaultResponse();
    }

    /**
     * 觸發爬取表特版
     */
    @GetMapping(value = "/beauty")
    public Result crawlerBeauty(@RequestParam(value = "pageSize", defaultValue = "2") Integer pageSize) {
        beautyCrawlerService.crawler(pageSize);
        return Result.getDefaultResponse();
    }
}
