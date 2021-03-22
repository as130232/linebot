package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.FlexGravity;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            //TODO currentPicture新增至DB
            //return new TextMessage("儲存成功。");
        }
        if (parameters.contains("size")) {
            return new TextMessage("圖片資源size:" + beautyCrawlerService.listPicture.size());
        }
        if (parameters.contains("上一張") && currentPicture != null) {
            URI uri = URI.create(currentPicture);
            return new ImageMessage(uri, uri);
        }
        if (parameters.contains("爬")) {
            beautyCrawlerService.crawler(1);
        }
        if (parameters.contains("多") || parameters.contains("more")) {
            Set<String> pictures = randomListPicture(10);
            if (pictures.size() == 0) {
                beautyCrawlerService.crawler(2);
                return new TextMessage("圖片為空，重新取得圖片資源中，請稍後(一分鐘)。");
            }
//            List<ImageCarouselColumn> columns = new ArrayList<>(10);
//            Integer i = 1;
//            for (String picture : pictures) {
//                URI uri = URI.create(picture);
//                ImageCarouselColumn carousel = new ImageCarouselColumn(uri, new URIAction("連結", uri, new URIAction.AltUri(uri)));
//                columns.add(carousel);
//                i++;
//            }
//            ImageCarouselTemplate carouselTemplate = new ImageCarouselTemplate(columns);
//            return new TemplateMessage("表特版精選", carouselTemplate);
            List<Bubble> listBubble = pictures.stream().map(picture -> {
                URI uri = URI.create(picture);
                List<FlexComponent> bodyContents = Arrays.asList(Image.builder().size(Image.ImageSize.FULL_WIDTH).margin(FlexMarginSize.MD).aspectMode(Image.ImageAspectMode.Cover).aspectRatio(2, 3).gravity(FlexGravity.TOP)
                        .url(uri).action(new URIAction("URL", uri, new URIAction.AltUri(uri))).build());
                Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll("0px").build();
                Bubble bubble = Bubble.builder().header(null).hero(null).body(body).footer(null).build();
                return bubble;
            }).collect(Collectors.toList());
            FlexContainer contents = Carousel.builder().contents(listBubble).build();
            return new FlexMessage("表特精選", contents);
        }
        if (parameters.contains("refresh"))
            beautyCrawlerService.init(); //重新取得圖片資源
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
        while (result.size() != size && beautyCrawlerService.listPicture.size() > size)
            result.add(beautyCrawlerService.randomPicture());
        return result;
    }
}
