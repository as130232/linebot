package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {
    private BeautyCrawlerService beautyCrawlerService;

    @Autowired
    public CrawlerController(BeautyCrawlerService beautyCrawlerService) {
        this.beautyCrawlerService = beautyCrawlerService;
    }

    @GetMapping(value = "/beauty")
    public Result crawlerBeauty(@RequestParam(value = "pageSize", defaultValue = "2") Integer pageSize) {
        beautyCrawlerService.crawler(pageSize);
        return Result.getDefaultResponse();
    }
}
