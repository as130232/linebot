package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command({"@close"})
public class CloseResidentCommandHandler implements CommandHandler {
    @Override
    public Message execute(String parameters) {
        return new TextMessage("[已關閉常駐指令模式]");
    }
}
