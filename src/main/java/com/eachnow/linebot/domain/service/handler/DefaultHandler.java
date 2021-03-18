package com.eachnow.linebot.domain.service.handler;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.handler.location.LocationHandler;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command({"@default"})
public class DefaultHandler implements CommandHandler, LocationHandler {
    @Override
    public Message execute(String parameters) {
        return null;
    }

    @Override
    public Message execute(LocationMessageContent content) {
        return null;
    }
}
