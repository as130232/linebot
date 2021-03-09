package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.domain.service.line.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/linebot")
public class LinebotController {
    private MessageSender messageSender;

    @Autowired
    public LinebotController(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @PostMapping(value = "/message/text/push")
    public void messageTextPush(@RequestParam(value = "type") String type, @RequestParam(value = "text") String text) throws Exception {
        String to = "Uf52a57f7e6ba861c05be8837bfbcf0c6";
        messageSender.send(to, type, text);
    }

    @PostMapping(value = "/messagePush")
    public void messagePush(@RequestBody PushMessagePO pushMessagePO) throws Exception {
        pushMessagePO.setTo("Uf52a57f7e6ba861c05be8837bfbcf0c6");
        messageSender.send(pushMessagePO);
    }

}
