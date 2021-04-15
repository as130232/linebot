package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.domain.service.gateway.LineApiService;
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
    private LineApiService lineApiService;

    @Autowired
    public LinebotController(MessageSender messageSender,
                             LineUserService lineUserService,
                             LineApiService lineApiService) {
        this.messageSender = messageSender;
        this.lineUserService = lineUserService;
        this.lineApiService = lineApiService;
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
     *
     * @param code
     * @param state
     * @throws Exception
     */
    @PostMapping(value = "/notify/subscribe")
    public void lineNotifySubscribe(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state) throws Exception {
        log.info("--lineNotifySubscribe--");
        String token = lineApiService.getLineNotifyToken(code);
        if (token != null) {
            lineUserService.updateNotifyToken(state, token);
        }
    }
}
