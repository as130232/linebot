package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.crawler.JavdbCrawlerService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    private MessageHandler messageHandler;
    private LineUserService lineUserService;
    private BeautyCrawlerService beautyCrawlerService;
    private JavdbCrawlerService javdbCrawlerService;

    @Autowired
    public TestController(MessageHandler messageHandler,
                          LineUserService lineUserService,
                          BeautyCrawlerService beautyCrawlerService,
                          JavdbCrawlerService javdbCrawlerService) {
        this.messageHandler = messageHandler;
        this.lineUserService = lineUserService;
        this.beautyCrawlerService = beautyCrawlerService;
        this.javdbCrawlerService = javdbCrawlerService;
    }

    @GetMapping(value = "/preventDormancy")
    public void preventDormancy() {
    }

    @GetMapping(value = "/command")
    public Result<String> testCommand(@RequestParam(value = "text") String text) throws JsonProcessingException {
        Result<String> result = new Result<>();
        Message message = messageHandler.executeCommand(null, text, null);
        result.setData(JsonUtils.toJsonString(message));
        return result;
    }

    @GetMapping(value = "/insertUser")
    public String insertUser() {
        String uuid = UUID.randomUUID().toString().substring(0, 33);
        lineUserService.saveLineUser(uuid);
        return uuid;
    }

    @GetMapping(value = "/crawlerRankings")
    public void crawlerRankings() {
        javdbCrawlerService.crawlerRankings(JavdbCrawlerService.TYPE_DAILY);
    }

}
