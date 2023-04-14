package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.domain.service.gateway.LineApiService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/telegram")
public class TelegramController {

    @Autowired
    public TelegramController() {
    }

    @PostMapping(value = "/message/text/push")
    public void messageTextPush(@RequestBody Object obj) throws Exception {
        log.info("telegram message:{}", obj);
    }

}
