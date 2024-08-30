package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.db.po.LineUserPO;
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

import java.util.Optional;

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
        switch (commandPO.getCommand()) {
            case "打卡":
                //設置打卡提醒
                String userName = null;
                Optional<LineUserPO> optional = lineUserService.getUser(commandPO.getUserId());
                if (optional.isPresent()) {
                    LineUserPO user = optional.get();
                    femasService.remindPunchOutByUser(user);
                    userName = user.getName();
                }
                //若有給名稱參數，則以名稱參數為主
                String userNameByParam = commandPO.getParams().get(0);
                if (userNameByParam == null || userNameByParam.equals("")) {
                    userName = userNameByParam;
                }
                FemasPunchRecordPO po = localCacheService.getPunchRecord(DateUtils.getCurrentDate(), userName);
                String sb = "上班時間：" + po.getPunchIn() + "\n" +
                        "下班時間：" + po.getPunchOut();
                return new TextMessage(sb);
        }
        return null;
    }
}
