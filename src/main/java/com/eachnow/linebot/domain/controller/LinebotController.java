package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.domain.service.line.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/linebot")
public class LinebotController {
    private MessageSender messageSender;

    @Autowired
    public LinebotController(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

//    @PostMapping(value = "/callback")
//    public void callback(@RequestBody Object object) {
//        System.out.println("object:" + object);
//    }

    @PostMapping(value = "/messagePush")
    public void messagePush(@RequestParam(value = "type") String type, @RequestParam(value = "text") String text) throws Exception {
        String to = "Uf52a57f7e6ba861c05be8837bfbcf0c6";
        messageSender.send(to, type, text);
    }
}
