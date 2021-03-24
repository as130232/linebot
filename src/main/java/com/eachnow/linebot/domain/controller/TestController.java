package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.db.repository.LineUserRepository;
import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    private MessageHandler messageHandler;
    private BeautyCrawlerService beautyCrawlerService;
    private LineUserRepository lineUserRepository;
    @Autowired
    public TestController(MessageHandler messageHandler, BeautyCrawlerService beautyCrawlerService,
                          LineUserRepository lineUserRepository) {
        this.messageHandler = messageHandler;
        this.beautyCrawlerService = beautyCrawlerService;
        this.lineUserRepository = lineUserRepository;
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


    @GetMapping(value = "/insertUser")
    public String insertUser() {
        String uuid = UUID.randomUUID().toString().substring(0, 32);
        lineUserRepository.save(LineUserPO.builder().id(uuid).createTime(new Timestamp(Instant.now().toEpochMilli())).build());
        return uuid;
    }
}
