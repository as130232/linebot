package com.eachnow.linebot.domain.service.handler.location.impl;

import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.constant.LocationConstants;
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

import java.net.URI;
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
        ResultLocationPO resultLocationPO = googleApiService.getLocation(String.valueOf(content.getLatitude()), String.valueOf(content.getLongitude()), LocationConstants.RESTAURANT, LanguageEnum.TW.getLang());
        //排序，評分高的在前，並指定推薦餐廳數量
        List<ResultPO> results = resultLocationPO.getResults().stream().sorted(Comparator.comparing(ResultPO::getRating).reversed()).limit(MAX_SIZE).collect(Collectors.toList());
        List<CarouselColumn> columns = results.stream().map(po -> {
            //取得餐廳的圖片網址
            URI imageUrl = null;
            if (po.getPhotos() != null && po.getPhotos().size() > 0 && po.getPhotos().get(0).getPhotoReference() != null)
                imageUrl = URI.create(googleApiService.parseMapPictureUrl(po.getPhotos().get(0).getPhotoReference()));
            //取得餐廳的 Google map 網址
            URI googleMapUrl = URI.create(googleApiService.parseMapUrl(po.getGeometry().getLocation().getLat(), po.getGeometry().getLocation().getLng(), po.getPlaceId()));
            String detail = String.format("評分:%s, 評論:%s\n地址:%s", po.getRating(), po.getUserRatingsTotal(), po.getVicinity());
            List<Action> actions = Arrays.asList(new URIAction("地圖", googleMapUrl, new URIAction.AltUri(googleMapUrl)));
            CarouselColumn carousel = new CarouselColumn(imageUrl, po.getName(), detail, actions);
            return carousel;
        }).collect(Collectors.toList());
        CarouselTemplate carouselTemplate = new CarouselTemplate(columns);
        //處理完需清空暫存類型
        LocationHandlerFactory.type = null;
        return new TemplateMessage("餐廳精選", carouselTemplate);
    }

}
