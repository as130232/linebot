package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command({"@default"})
public class DefaultHandler implements CommandHandler {
    @Override
    public Message execute(String parameters) {
        return null;
    }
}
