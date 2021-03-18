package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.LocationConstants;
import com.eachnow.linebot.common.util.LineTemplateUtils;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.handler.location.LocationHandlerFactory;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Command({"restaurant", "餐廳", "餓"})
public class RestaurantHandler implements CommandHandler {
    private BeautyCrawlerService beautyCrawlerService;
    private GoogleApiService googleApiService;

    @Autowired
    public RestaurantHandler(BeautyCrawlerService beautyCrawlerService,
                             GoogleApiService googleApiService) {
        this.beautyCrawlerService = beautyCrawlerService;
        this.googleApiService = googleApiService;
    }

    @Override
    public Message execute(String parameters) {
        LocationHandlerFactory.type = LocationConstants.RESTAURANT;
        return LineTemplateUtils.getLocationButtonsTemplate();
    }

}
