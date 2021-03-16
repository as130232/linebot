package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.*;

@Slf4j
@Command({"抽", "beauty", "表特"})
public class BeautyHandler implements CommandHandler {
    private BeautyCrawlerService beautyCrawlerService;
    private String currentPicture;

    @Autowired
    public BeautyHandler(BeautyCrawlerService beautyCrawlerService) {
        this.beautyCrawlerService = beautyCrawlerService;
    }

    @Override
    public Message execute(String parameters) {
        if (parameters.contains("存")) {
            //TODO 新增至DB
            return new TextMessage("儲存成功。");
        }
        if (parameters.contains("size")) {
            return new TextMessage("圖片資源size:" + beautyCrawlerService.listPicture.size());
        }
        if (parameters.contains("上一張") && currentPicture != null) {
            URI uri = URI.create(currentPicture);
            return new ImageMessage(uri, uri);
        }
        if (parameters.contains("多")) {
            List<CarouselColumn> columns = new ArrayList<>(10);
            Set<String> pictures = randomListPicture(10);
            if (pictures.size() == 0) {
                beautyCrawlerService.crawler(2);
                return new TextMessage("圖片為空，重新取得圖片資源中，請稍後(一分鐘)。");
            }
            for (String picture : pictures) {
                URI uri = URI.create(picture);
                List<Action> actions = Arrays.asList(
                        new PostbackAction("測試", "test"),
                        new URIAction("連結", uri, new URIAction.AltUri(uri)));
                CarouselColumn carousel = CarouselColumn.builder().title("表特").text("表特文本").thumbnailImageUrl(uri).actions(actions).build();
                columns.add(carousel);
            }
            CarouselTemplate carouselTemplate = CarouselTemplate.builder().columns(columns).build();
            return new TemplateMessage("表特版精選", carouselTemplate);
        }
        if (parameters.contains("refresh")) {
            beautyCrawlerService.init(); //重新取得圖片資源
        }
        if (beautyCrawlerService.listPicture.size() == 0) {
            beautyCrawlerService.crawler(2);
            return new TextMessage("重新取得圖片資源中，請稍後(一分鐘)。");
        }
        String pictureUrl = beautyCrawlerService.randomPicture();
        currentPicture = pictureUrl;    //紀錄當前圖片
        URI uri = URI.create(pictureUrl);
        return new ImageMessage(uri, uri);
    }

    private Set<String> randomListPicture(int size) {
        Set<String> result = new HashSet<>(size);
        while (result.size() != size)
            result.add(beautyCrawlerService.randomPicture());
        return result;
    }
}
