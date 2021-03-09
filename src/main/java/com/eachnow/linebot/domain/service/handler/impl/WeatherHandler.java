package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command({"天氣"})
public class WeatherHandler implements CommandHandler {

    @Override
    public Message execute(String parameters) {
        log.info("天氣狀況");
        return new TextMessage("天氣狀況");
    }
}
