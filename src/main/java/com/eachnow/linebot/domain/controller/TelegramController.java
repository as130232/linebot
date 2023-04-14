package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.common.po.telegram.TelegramResultPO;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.domain.service.gateway.LineApiService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.eachnow.linebot.domain.service.line.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * telegram有兩種獲取用戶訊息方式
 * 1.主動獲取，打API輪詢方式
 * https://api.telegram.org/bot{bot_token}/getUpdates
 * 2.被動獲取，當有新訊息telegram會推播至URL後的API
 * https://api.telegram.org/bot{bot_token}/setWebhook?url=https://linebotmuyu.herokuapp.com/telegram/message/text/push
 */
@Slf4j
@RestController
@RequestMapping("/telegram")
public class TelegramController {

    @Autowired
    public TelegramController() {
    }

    @PostMapping(value = "/message/text/push")
    public void messageTextPush(@RequestBody TelegramResultPO po) throws Exception {
        log.info("telegram message:{}", JsonUtils.toJsonString(po));
    }

}
