package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.crawler.ActressCrawlerService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

@Slf4j
@Command({"女優", "av"})
public class ActressHandler implements CommandHandler {
    private ActressCrawlerService actressCrawlerService;
    private String currentPicture;

    @Autowired
    public ActressHandler(ActressCrawlerService actressCrawlerService) {
        this.actressCrawlerService = actressCrawlerService;
    }

    @Override
    public Message execute(String parameters) {
        if (parameters.contains("存")) {
            //TODO 新增至DB
            return new TextMessage("儲存成功。");
        }
        if (parameters.contains("size")) {
            return new TextMessage("圖片資源size:" + actressCrawlerService.listPicture.size());
        }
        if (parameters.contains("上一張") && currentPicture != null) {
            URI uri = URI.create(currentPicture);
            return new ImageMessage(uri, uri);
        }
        if (parameters.contains("refresh")) {
            actressCrawlerService.init(); //重新取得圖片資源
        }
        if (actressCrawlerService.listPicture.size() == 0) {
            actressCrawlerService.crawler(2);
            return new TextMessage("重新取得圖片資源中，請稍後(一分鐘)。");
        }
        String pictureUrl = actressCrawlerService.randomPicture();
        currentPicture = pictureUrl;
        URI uri = URI.create(pictureUrl);
        return new ImageMessage(uri, uri);
    }
}
