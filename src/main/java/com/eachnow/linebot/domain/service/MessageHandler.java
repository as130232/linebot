package com.eachnow.linebot.domain.service;

import com.eachnow.linebot.common.util.CommandUtils;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.eachnow.linebot.domain.service.handler.CommandHandlerFactory;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@LineMessageHandler
public class MessageHandler {
    private CommandHandlerFactory handlerCommandFactory;

    @Autowired
    public MessageHandler(CommandHandlerFactory handlerCommandFactory) {
        this.handlerCommandFactory = handlerCommandFactory;
    }

    public Message executeCommand(String text) {
        String command = CommandUtils.parseCommand(text);
        CommandHandler commandHandler = handlerCommandFactory.getCommandHandler(command);
        return commandHandler.execute(text);
    }

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        System.out.println("event: " + event);
        final String text = event.getMessage().getText();
        //根據指令取得對應指令處理服務
        return this.executeCommand(text);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("handleDefaultMessageEvent，event: " + event);
    }
}
