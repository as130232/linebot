package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    private MessageHandler messageHandler;
    private BeautyCrawlerService beautyCrawlerService;

    @Autowired
    public TestController(MessageHandler messageHandler, BeautyCrawlerService beautyCrawlerService) {
        this.messageHandler = messageHandler;
        this.beautyCrawlerService = beautyCrawlerService;
    }

    @GetMapping(value = "/preventDormancy")
    public void preventDormancy() {
        log.info("---preventDormancy---");
    }

    @GetMapping(value = "/command")
    public Result testCommand(@RequestParam(value = "text") String text) {
        Result<String> result = new Result<>();
        result.setData("test by charles:" + messageHandler.executeCommand(text));
        return result;
    }

    @GetMapping(value = "/picture")
    public String randomPicture() {
        return beautyCrawlerService.randomPicture();
    }

}
