package com.eachnow.linebot.domain.service.handler;
import com.linecorp.bot.model.message.Message;
public interface CommandHandler {
    public Message execute(String parameters);
}
