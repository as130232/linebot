package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DescriptionCommandPO;
import com.eachnow.linebot.common.po.DescriptionPO;
import com.eachnow.linebot.common.po.PttArticlePO;
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
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Command({"抽", "beauty", "表特"})
public class BeautyHandler implements CommandHandler {
    private BeautyCrawlerService beautyCrawlerService;
    private String currentPicture;
    public static final String DefaultImage = "https://i.imgur.com/ht8dxBm.jpg";

    @Autowired
    public BeautyHandler(BeautyCrawlerService beautyCrawlerService) {
        this.beautyCrawlerService = beautyCrawlerService;
    }

    public static DescriptionPO getDescription() {
        List<DescriptionCommandPO> commands = new ArrayList<>();
        commands.add(DescriptionCommandPO.builder().explain("隨機抽取表特版圖片").command("抽").postback("抽").build());
        commands.add(DescriptionCommandPO.builder().explain("隨機精選十張表特版圖片").command("抽 多").postback("抽 多").build());
        return DescriptionPO.builder().title("表特").description("爬取PTT表特版圖片資源。")
                .commands(commands).imageUrl(DefaultImage).build();
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        if (text.contains("存")) {
            //TODO currentPicture新增至DB
            //return new TextMessage("儲存成功。");
        }
        if (text.contains("size")) {
            return new TextMessage("圖片資源size:" + beautyCrawlerService.articleMap.size());
        }
        if (text.contains("上一張") && currentPicture != null) {
            URI uri = URI.create(currentPicture);
            return new ImageMessage(uri, uri);
        }
        if (text.contains("爬")) {
            beautyCrawlerService.crawler(3);
        }
        if (text.contains("多") || text.contains("more")) {
            Set<PttArticlePO> pictures = beautyCrawlerService.listArticle(10);
            if (pictures.size() == 0) {
                beautyCrawlerService.crawler(10);
                return new TextMessage("圖片為空，重新取得圖片資源中，請稍後(一分鐘)。");
            }
            List<Bubble> listBubble = pictures.stream().map(po -> {
                URI articleUri = URI.create(po.getWebUrl());
                Box article = Box.builder().layout(FlexLayout.VERTICAL).position(FlexPosition.ABSOLUTE).backgroundColor("#ff334b")
                        .cornerRadius("20px").offsetTop("18px").offsetStart("18px").height("25px").width("53px").contents(Arrays.asList(
                                Text.builder().text("文章").color("#ffffff").align(FlexAlign.CENTER).size(FlexFontSize.XS).offsetTop("3px").build()
                        )).action(new URIAction("URL", articleUri, new URIAction.AltUri(articleUri))).build();
                String picture = DefaultImage;
                if (!po.getPictures().isEmpty()) {
                    picture = po.getPictures().iterator().next();
                }
                URI uri = URI.create(picture);
                List<FlexComponent> bodyContents = Arrays.asList(
                        Image.builder().size(Image.ImageSize.FULL_WIDTH).aspectMode(Image.ImageAspectMode.Cover).aspectRatio(5, 7)
                                .url(uri).action(new URIAction("URL", uri, new URIAction.AltUri(uri))).build(), article);
                Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.NONE).build();
                return Bubble.builder().header(null).hero(null).body(body).footer(null).build();
            }).collect(Collectors.toList());
            FlexContainer contents = Carousel.builder().contents(listBubble).build();
            return new FlexMessage("表特精選", contents);
        }
        if (text.contains("refresh"))
            beautyCrawlerService.crawler(10); //重新取得圖片資源
        if (beautyCrawlerService.articleMap.size() == 0) {
            beautyCrawlerService.crawler(2);
//            return new TextMessage("重新取得圖片資源中，請稍後(一分鐘)。");
        }
        String pictureUrl = beautyCrawlerService.randomPicture();
        currentPicture = pictureUrl;    //紀錄當前圖片
        URI uri = URI.create(pictureUrl);
        return new ImageMessage(uri, uri);
    }

}
