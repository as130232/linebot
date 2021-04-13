package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/linebot")
public class LinebotController {
    private MessageSender messageSender;
    private LineUserService lineUserService;

    @Autowired
    public LinebotController(MessageSender messageSender,
                             LineUserService lineUserService) {
        this.messageSender = messageSender;
        this.lineUserService = lineUserService;
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

    /**
     * 取得授權碼
     * @param code
     * @param state
     * @throws Exception
     */
    @PostMapping(value = "/notify/subscribe")
    public void lineNotifySubscribe(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state) throws Exception {
        log.info("--lineNotifySubscribe--");
        lineUserService.updateNotifyToken(state, code);
    }

    /**
     * 取得存取權杖
     * @throws Exception
     */
    @PostMapping(value = "/notify/access")
    public void lineNotifyAccess(@RequestParam(value = "message") String message, @RequestParam(value = "access_token") String accessToken) throws Exception {
        log.info("--lineNotifyAccess--");
//        lineUserService.updateNotifyToken(state, code);
    }
}
