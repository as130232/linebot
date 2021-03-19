package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.common.util.LineTemplateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.crawler.ActressCrawlerService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.handler.location.LocationHandlerFactory;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Command({"place", "location", "地點", "地方"})
public class PlaceHandler implements CommandHandler {
    private ActressCrawlerService actressCrawlerService;
    private String currentPicture;

    @Autowired
    public PlaceHandler(ActressCrawlerService actressCrawlerService) {
        this.actressCrawlerService = actressCrawlerService;
    }

    @Override
    public Message execute(String parameters) {
        GooglePlaceTypeEnum typeEnum = GooglePlaceTypeEnum.parse(ParamterUtils.getParameter(parameters));
        if (typeEnum == null)
            return new TextMessage("Incorrect location. Please input again.\n找不到該地點請重新輸入.");
        LocationHandlerFactory.type = typeEnum;
        return LineTemplateUtils.getLocationButtonsTemplate(typeEnum.getName());
    }

}
