package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Arrays;

@Slf4j
@Command({"restaurant", "餐廳", "餓"})
public class RestaurantHandler implements CommandHandler {
    private BeautyCrawlerService beautyCrawlerService;
    private String currentPicture;

    @Autowired
    public RestaurantHandler(BeautyCrawlerService beautyCrawlerService) {
        this.beautyCrawlerService = beautyCrawlerService;
    }

    @Override
    public Message execute(String parameters) {
        String text = "Please tell me where you are?";
        URI uri = URI.create("https://line.me/R/nv/location");
        ButtonsTemplate template = new ButtonsTemplate(uri, "Location", "Please tell me where you are", Arrays.asList(
                new URIAction("Send my location", uri, new URIAction.AltUri(uri))));
        return new TemplateMessage(text, template);
    }
}
