package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.MessageHandler;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/setting")
public class SettingController {
    private MessageHandler messageHandler;
    private BeautyCrawlerService beautyCrawlerService;
    @Autowired
    public SettingController(MessageHandler messageHandler, BeautyCrawlerService beautyCrawlerService) {
        this.messageHandler = messageHandler;
        this.beautyCrawlerService = beautyCrawlerService;
    }

    /**
     * 取得排程開關狀態
     */
    @GetMapping(value = "/test")
    public Result test(@RequestParam(value = "text") String text) {
        Result<String> result = new Result<>();
        result.setData("test by charles:" + messageHandler.executeCommand(text));
        return result;
    }

    @GetMapping(value = "/picture")
    public String randomPicture() {
        return beautyCrawlerService.randomPicture();
    }
}
