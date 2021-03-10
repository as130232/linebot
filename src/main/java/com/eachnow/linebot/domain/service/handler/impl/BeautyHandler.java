package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Command({"抽", "beauty", "表特"})
public class BeautyHandler implements CommandHandler {
    private Set<String> listPicture = new HashSet<>(500);
    private BeautyCrawlerService beautyCrawlerService;

    @Autowired
    public BeautyHandler(BeautyCrawlerService beautyCrawlerService) {
        this.beautyCrawlerService = beautyCrawlerService;
    }

    @Override
    public Message execute(String parameters) {
        if (listPicture.size() == 0) {
            beautyCrawlerService.crawler(1);
            return new TextMessage("重新取得圖片資源中，請稍後。");
        }
        if (parameters.contains("refresh"))
            beautyCrawlerService.init();
        URI uri = URI.create(beautyCrawlerService.randomPicture());
        return new ImageMessage(uri, uri);
    }

}
