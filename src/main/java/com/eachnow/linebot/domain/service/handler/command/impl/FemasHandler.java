package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.LineTemplateUtils;
import com.eachnow.linebot.domain.service.FemasService;
import com.eachnow.linebot.domain.service.LocalCacheService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.LineUserService;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Command({"打卡", "femas"})
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
            case "打卡": {
                //設置打卡提醒
                Optional<LineUserPO> optional = lineUserService.getUser(commandPO.getUserId());
                if (!optional.isPresent()) {
                    return new TextMessage("找不到該用戶，請註冊用戶資料: userId:" + commandPO.getUserId());
                }
                LineUserPO user = optional.get();
                femasService.remindPunchOutByUser(user);
                String userName = user.getName();
                ZonedDateTime today = DateUtils.parseDate(DateUtils.getCurrentDate(), DateUtils.yyyyMMddDash);
                String searchStart = today.minusDays(1).format(DateUtils.yyyyMMddDash); //前一天
                String searchEnd = today.format(DateUtils.yyyyMMddDash);
                FemasPunchRecordPO po = femasService.getPunchRecordAndSetCache(userName, user.getFemasToken(), searchStart, searchEnd);
                if (po == null) {
                    return new TextMessage("未有打卡記錄。");
                }
                String sb = "上班時間：" + po.getPunchIn() + "\n" +
                        "下班時間：" + po.getPunchOut();
                return new TextMessage(sb);
            }
            case "femas": {
                String femasToken = commandPO.getParams().get(0);
                lineUserService.updateFemasToken(commandPO.getUserId(), femasToken);
                return LineTemplateUtils.getSuccessTemplate("更新用戶 femas Token 成功");
            }
        }
        return null;
    }
}
