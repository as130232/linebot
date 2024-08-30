package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.FemasService;
import com.eachnow.linebot.domain.service.LocalCacheService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Command({"打卡"})
public class FemasHandler implements CommandHandler {
    private final LocalCacheService localCacheService;
    private final FemasService femasService;
    private final LineUserService lineUserService;

    @Autowired
    public FemasHandler(LocalCacheService localCacheService, FemasService femasService, LineUserService lineUserService) {
        this.localCacheService = localCacheService;
        this.femasService = femasService;
        this.lineUserService = lineUserService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String userName = lineUserService.getName(commandPO.getUserId());
        String userNameByParam = commandPO.getParams().get(0);
        if (userNameByParam == null || userNameByParam.equals("")) {
            userName = userNameByParam;
        }
        switch (commandPO.getCommand()) {
            case "打卡":
                femasService.remindPunchOut();
                FemasPunchRecordPO po = localCacheService.getPunchRecord(DateUtils.getCurrentDate(), userName);
                String sb = "上班時間：" + po.getPunchIn() + "\n" +
                        "下班時間：" + po.getPunchOut();
                return new TextMessage(sb);
        }
        return null;
    }
}
