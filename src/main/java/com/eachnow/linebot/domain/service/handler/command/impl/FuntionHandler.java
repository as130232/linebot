package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command({"funtion", "功能"})
public class FuntionHandler implements CommandHandler {
    @Override
    public Message execute(CommandPO commandPO) {
        //表特

        //地點

        //翻譯

        //匯率(幣值轉換)

        //記帳

        //天氣

        //吃什麼、你好

        //股票 待做

        return null;
    }
}
