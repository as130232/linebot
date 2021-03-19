package com.eachnow.linebot.domain.service.handler.location.impl;

import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.po.google.map.ResultLocationPO;
import com.eachnow.linebot.common.po.google.map.ResultPO;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import com.eachnow.linebot.domain.service.handler.location.LocationHandler;
import com.eachnow.linebot.domain.service.handler.location.LocationHandlerFactory;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoogleLocationHandler implements LocationHandler {
    private final Integer MAX_SIZE = 10;
    private GoogleApiService googleApiService;

    @Autowired
    public GoogleLocationHandler(GoogleApiService googleApiService) {
        this.googleApiService = googleApiService;
    }

    @Override
    public Message execute(LocationMessageContent content) {
        GooglePlaceTypeEnum typeEnum = LocationHandlerFactory.type;
        ResultLocationPO resultLocationPO = googleApiService.getLocation(String.valueOf(content.getLatitude()), String.valueOf(content.getLongitude()), typeEnum, LanguageEnum.TW.getLang());
        //排序，評分高、評論高的在前，並指定上限數量
        List<ResultPO> results = resultLocationPO.getResults().stream()
                .sorted(Comparator.comparing(ResultPO::getRating).thenComparing(ResultPO::getUserRatingsTotal).reversed())
                .limit(MAX_SIZE).collect(Collectors.toList());
        if (results.size() == 0)
            return null;
        List<CarouselColumn> columns = results.stream().map(po -> {
            //取得餐廳的圖片網址
            URI imageUrl = URI.create("https://i.imgur.com/R0qpw6h.jpg");   //default
            if (po.getPhotos() != null && po.getPhotos().size() > 0 && po.getPhotos().get(0).getPhotoReference() != null)
                imageUrl = URI.create(googleApiService.parseMapPictureUrl(po.getPhotos().get(0).getPhotoReference()));
            //取得餐廳的 Google map 網址
            URI googleMapUrl = URI.create(googleApiService.parseMapUrl(po.getGeometry().getLocation().getLat(), po.getGeometry().getLocation().getLng(), po.getPlaceId()));
            String detail = String.format("評分:%s, 評論:%s  |  %s\n地址:%s", po.getRating(), po.getUserRatingsTotal(), po.getOpeningHours().getOpenNow() ? "營業中" : "休息中",
                    po.getVicinity());
            List<Action> actions = Arrays.asList(new URIAction("地圖", googleMapUrl, new URIAction.AltUri(googleMapUrl)));
            String title = po.getName();
            if (title.length() > 40) {  //title有字數限制
                String[] arr = title.split(",");
                title = arr[arr.length - 1];
            }
            CarouselColumn carousel = new CarouselColumn(imageUrl, title, detail, actions);
            return carousel;
        }).collect(Collectors.toList());
        CarouselTemplate carouselTemplate = new CarouselTemplate(columns);
        return new TemplateMessage(typeEnum.getName() + " 精選", carouselTemplate);
    }

//    @PostConstruct
//    private void test() {
//        LocationHandlerFactory.type = GooglePlaceTypeEnum.CITY_HALL;
//        LocationMessageContent content = LocationMessageContent.builder().id("13744078637930")
//                .address("114台灣台北市內湖區港墘路187號")
//                .latitude(25.075796639438316).longitude(121.57483376562594).build();
//        this.execute(content);
//    }
}
