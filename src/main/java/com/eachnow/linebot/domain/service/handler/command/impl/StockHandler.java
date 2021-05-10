package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Command({"stock", "股", "股票"})
public class StockHandler implements CommandHandler {

    @Autowired
    public StockHandler() {
    }

    @Override
    public Message execute(CommandPO commandPO) {
        //Todo 紀錄該股並自動換算停利停損價格

        //健檢該檔本益比、成長率、

        //指數，取得大盤及各類指數

        return null;
    }

}
