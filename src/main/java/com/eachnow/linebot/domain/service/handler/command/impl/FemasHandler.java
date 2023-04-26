package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.LocalCacheService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Command({"打卡"})
public class FemasHandler implements CommandHandler {
    private final LocalCacheService localCacheService;

    @Autowired
    public FemasHandler(LocalCacheService localCacheService) {
        this.localCacheService = localCacheService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        switch (commandPO.getCommand()) {
            case "打卡":
                FemasPunchRecordPO po = localCacheService.getPunchRecord(DateUtils.getCurrentDate());
                String sb = "上班時間：" + po.getStartTime() + "\n" +
                        "下班時間：" + po.getEndTime();
                return new TextMessage(sb);
        }
        return null;
    }
}
