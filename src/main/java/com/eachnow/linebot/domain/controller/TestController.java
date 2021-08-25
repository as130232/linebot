package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageHandler;
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
    private BeautyCrawlerService beautyCrawlerService;
    private LineUserService lineUserService;

    @Autowired
    public TestController(MessageHandler messageHandler, BeautyCrawlerService beautyCrawlerService,
                          LineUserService lineUserService) {
        this.messageHandler = messageHandler;
        this.beautyCrawlerService = beautyCrawlerService;
        this.lineUserService = lineUserService;
    }

    @GetMapping(value = "/preventDormancy")
    public void preventDormancy() {}

    @GetMapping(value = "/command")
    public Result testCommand(@RequestParam(value = "text") String text) {
        Result<String> result = new Result<>();
        result.setData("test by charles:" + messageHandler.executeCommand(null, text, null));
        return result;
    }

    @GetMapping(value = "/picture")
    public String randomPicture() {
        return beautyCrawlerService.randomPicture().getPictureUrl();
    }

    @GetMapping(value = "/insertUser")
    public String insertUser() {
        String uuid = UUID.randomUUID().toString().substring(0, 33);
        lineUserService.saveLineUser(uuid);
        return uuid;
    }
}
