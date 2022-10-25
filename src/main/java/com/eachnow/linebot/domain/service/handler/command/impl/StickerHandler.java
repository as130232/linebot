package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;

@Command({"sticker", "貼圖"})
public class StickerHandler implements CommandHandler {
    @Override
    public Message execute(CommandPO commandPO) {
        String packageId = commandPO.getParams().get(0);
        String stickerId = commandPO.getParams().get(1);
        return new StickerMessage(packageId, stickerId);
    }
}
