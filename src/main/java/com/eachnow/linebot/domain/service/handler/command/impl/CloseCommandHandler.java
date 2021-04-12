package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.LineTemplateUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command({"@close", "@關"})
public class CloseCommandHandler implements CommandHandler {
    @Override
    public Message execute(CommandPO commandPO) {
        MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());
        return LineTemplateUtils.getCancelTemplate("已關閉常駐指令模式");
    }
}
