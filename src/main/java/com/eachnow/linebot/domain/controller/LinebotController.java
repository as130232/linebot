package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.common.po.line.LineCallbackPO;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.domain.service.gateway.LineApiService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.eachnow.linebot.domain.service.line.MessageSender;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/linebot")
public class LinebotController {
    private MessageSender messageSender;
    private MessageHandler messageHandler;
    private LineUserService lineUserService;
    private LineApiService lineApiService;

    @Autowired
    public LinebotController(MessageSender messageSender,
                             MessageHandler messageHandler,
                             LineUserService lineUserService,
                             LineApiService lineApiService) {
        this.messageSender = messageSender;
        this.messageHandler = messageHandler;
        this.lineUserService = lineUserService;
        this.lineApiService = lineApiService;
    }

    @PostMapping(value = "/message/text/push")
    public void messageTextPush(@RequestParam(value = "to", defaultValue = "Uf52a57f7e6ba861c05be8837bfbcf0c6") String to,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "text") String text) throws Exception {
        messageSender.pushByRest(to, type, text);
    }

    @PostMapping(value = "/messagePush")
    public void messagePush(@RequestBody PushMessagePO pushMessagePO) throws Exception {
        messageSender.pushByRest(pushMessagePO);
    }

    /**
     * Callback-取得授權碼，在根據授權碼取得對應line notify token
     *
     */
    @PostMapping(value = "/notify/subscribe")
    public ModelAndView lineNotifySubscribe(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state) throws Exception {
        log.info("--lineNotifySubscribe--");
        String token = lineApiService.getLineNotifyToken(code);
        if (token != null) {
            lineUserService.updateNotifyToken(state, token);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("remind/success.html");
            return modelAndView;
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("remind/failed.html");
        return modelAndView;
    }
}
