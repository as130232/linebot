package com.eachnow.linebot.domain.service.handler.command;
import com.eachnow.linebot.common.po.CommandPO;
import com.linecorp.bot.model.message.Message;
public interface CommandHandler {
    public Message execute(CommandPO commandPO);
}
