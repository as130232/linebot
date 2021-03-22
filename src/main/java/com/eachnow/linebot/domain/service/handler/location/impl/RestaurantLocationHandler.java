package com.eachnow.linebot.domain.service.handler.location.impl;

import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.po.google.map.ResultLocationPO;
import com.eachnow.linebot.common.po.google.map.ResultPO;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import com.eachnow.linebot.domain.service.handler.location.LocationHandler;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestaurantLocationHandler implements LocationHandler {
    private final Integer MAX_SIZE = 10;
    private GoogleApiService googleApiService;

    @Autowired
    public RestaurantLocationHandler(GoogleApiService googleApiService) {
        this.googleApiService = googleApiService;
    }

    @Override
    public Message execute(LocationMessageContent content) {
        ResultLocationPO resultLocationPO = googleApiService.getLocation(String.valueOf(content.getLatitude()), String.valueOf(content.getLongitude()), GooglePlaceTypeEnum.RESTAURANT, LanguageEnum.TW.getLang());
        //排序，評分高、評論高的在前，並指定推薦餐廳數量
        List<ResultPO> results = resultLocationPO.getResults().stream()
                .sorted(Comparator.comparing(ResultPO::getRating).thenComparing(ResultPO::getUserRatingsTotal).reversed())
                .limit(MAX_SIZE).collect(Collectors.toList());
        if (results.size() == 0)
            return null;
//        List<CarouselColumn> columns = results.stream().map(po -> {
//            //取得餐廳的圖片網址
//            URI imageUrl = URI.create("https://i.imgur.com/R0qpw6h.jpg");   //default
//            if (po.getPhotos() != null && po.getPhotos().size() > 0 && po.getPhotos().get(0).getPhotoReference() != null)
//                imageUrl = URI.create(googleApiService.parseMapPictureUrl(po.getPhotos().get(0).getPhotoReference()));
//            //取得餐廳的 Google map 網址
//            URI googleMapUrl = URI.create(googleApiService.parseMapUrl(po.getGeometry().getLocation().getLat(), po.getGeometry().getLocation().getLng(), po.getPlaceId()));
//            String detail = String.format("評分:%s, 評論:%s  |  %s\n地址:%s", po.getRating(), po.getUserRatingsTotal(), po.getOpeningHours().getOpenNow() ? "營業中" : "休息中",
//                    po.getVicinity());
//            List<Action> actions = Arrays.asList(new URIAction("地圖", googleMapUrl, new URIAction.AltUri(googleMapUrl)));
//            String title = po.getName();
//            if (title.length() > 40) {  //title有字數限制
//                String[] arr = title.split(",");
//                title = arr[arr.length - 1];
//            }
//            CarouselColumn carousel = new CarouselColumn(imageUrl, title, detail, actions);
//            return carousel;
//        }).collect(Collectors.toList());
//        CarouselTemplate carouselTemplate = new CarouselTemplate(columns);

        List<Bubble> listBubble = results.stream().map(po -> {
            //取得餐廳的圖片網址
            URI imageUrl = URI.create("https://i.imgur.com/R0qpw6h.jpg");   //default
            if (po.getPhotos() != null && po.getPhotos().size() > 0 && po.getPhotos().get(0).getPhotoReference() != null)
                imageUrl = URI.create(googleApiService.parseMapPictureUrl(po.getPhotos().get(0).getPhotoReference()));
            //取得餐廳的 Google map 網址
            URI googleMapUrl = URI.create(googleApiService.parseMapUrl(po.getGeometry().getLocation().getLat(), po.getGeometry().getLocation().getLng(), po.getPlaceId()));
            String title = po.getName();
            if (title.length() > 40) {  //title有字數限制
                String[] arr = title.split(",");
                title = arr[arr.length - 1];
            }
            FlexComponent hero = Image.builder().size(Image.ImageSize.FULL_WIDTH).aspectRatio(20, 13).aspectMode(Image.ImageAspectMode.Cover)
                    .url(imageUrl).action(new URIAction("地圖", googleMapUrl, new URIAction.AltUri(googleMapUrl))).build();
            String openNow = "休息中";
            if (po.getOpeningHours() != null && po.getOpeningHours().getOpenNow())
                openNow =  "營業中";
            Integer star = Math.round(po.getRating());
            List<FlexComponent> starContents = new ArrayList();
            for (int i = 0; i < star; i++)
                starContents.add(Icon.builder().size(FlexFontSize.SM).url(URI.create("https://scdn.line-apps.com/n/channel_devcenter/img/fx/review_gold_star_28.png")).build());
            starContents.add(Text.builder().text(String.valueOf(po.getRating())).color("#ff991a").flex(1).margin(FlexMarginSize.MD).weight(Text.TextWeight.BOLD).build());
            starContents.add(Text.builder().text(String.valueOf(po.getUserRatingsTotal())).color("#479AC7").weight(Text.TextWeight.BOLD).build());
            starContents.add(Text.builder().text(openNow).color(po.getOpeningHours().getOpenNow() ? "#00c72e" : "#ff291f").weight(Text.TextWeight.BOLD).build());

            List<FlexComponent> placeContents = Arrays.asList(
                    Text.builder().text("Place").color("#aaaaaa").size(FlexFontSize.SM).flex(1).build(),
                    Text.builder().text(po.getVicinity()).color("#666666").wrap(true).size(FlexFontSize.SM).flex(5).build()
            );
            List<FlexComponent> bodyContents = Arrays.asList(
                    Text.builder().text(title).weight(Text.TextWeight.BOLD).size(FlexFontSize.XL).build(),  //標頭
                    Box.builder().layout(FlexLayout.BASELINE).margin(FlexMarginSize.MD).contents(starContents).build(),
                    Box.builder().layout(FlexLayout.VERTICAL).margin(FlexMarginSize.LG).spacing(FlexMarginSize.SM).contents(placeContents).build()
            );
            Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build();
            Bubble bubble = Bubble.builder().header(null).hero(hero).body(body)
                    .footer(Box.builder().build()).action(new URIAction("地圖", googleMapUrl, new URIAction.AltUri(googleMapUrl))).build();
            return bubble;
        }).collect(Collectors.toList());
        FlexContainer contents = Carousel.builder().contents(listBubble).build();
        return new FlexMessage("餐廳精選", contents);
    }

}
