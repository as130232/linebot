package com.eachnow.linebot.common.util;

import com.eachnow.linebot.common.po.google.map.ResultLocationPO;
import com.eachnow.linebot.common.po.google.map.ResultPO;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.linecorp.bot.model.message.flex.unit.FlexOffsetSize;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class LineTemplateUtils {
    @Value("${google.api.key:}")
    private static String GOOGLE_API_KEY;
    private static long MAX_SIZE = 10;

    public static Message getLocationButtonsTemplate(String loaction) {
        String text = "Please tell me where you are?";
        String title = null;
        if (loaction != null)
            title = "Search: " + loaction;
        URI uri = URI.create("https://line.me/R/nv/location");
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text(title).weight(Text.TextWeight.BOLD).size(FlexFontSize.XL).build(),  //標頭
                Text.builder().text(text).margin(FlexMarginSize.SM).offsetTop(FlexOffsetSize.XS).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build();
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(new URIAction("Send my location", uri, new URIAction.AltUri(uri))).build()
        );
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(footerContents).build();
        FlexContainer contents = Bubble.builder().header(null).hero(null).body(body).footer(footer)
                .action(new URIAction("Send my location", uri, new URIAction.AltUri(uri))).build();
        return new FlexMessage("Search location", contents);
    }


    public static String parseMapUrl(String lat, String lng, String placeId) {
        return "https://www.google.com/maps/search/?api=1&query={lat},{lng}&query_place_id={placeId}"
                .replace("{lat}", lat).replace("{lng}", lng).replace("{placeId}", placeId);
    }

    public static String parseMapPictureUrl(String photoreference) {
        return "https://maps.googleapis.com/maps/api/place/photo?key={key}&photoreference={photoreference}&maxwidth=1024"
                .replace("{key}", GOOGLE_API_KEY).replace("{photoreference}", photoreference);
    }

    public static CarouselTemplate parseCarouselTemplate(ResultLocationPO resultLocationPO) {
        //排序，評分高、評論高的在前，並指定推薦餐廳數量
        List<ResultPO> results = resultLocationPO.getResults().stream()
                .sorted(Comparator.comparing(ResultPO::getRating).thenComparing(ResultPO::getUserRatingsTotal).reversed())
                .limit(MAX_SIZE).collect(Collectors.toList());
        if (results.size() == 0)
            return null;
        List<CarouselColumn> columns = results.stream().map(po -> {
            //取得餐廳的圖片網址
            URI imageUrl = URI.create("https://i.imgur.com/R0qpw6h.jpg");   //default
            if (po.getPhotos() != null && po.getPhotos().size() > 0 && po.getPhotos().get(0).getPhotoReference() != null)
                imageUrl = URI.create(parseMapPictureUrl(po.getPhotos().get(0).getPhotoReference()));
            //取得餐廳的 Google map 網址
            URI googleMapUrl = URI.create(parseMapUrl(po.getGeometry().getLocation().getLat(), po.getGeometry().getLocation().getLng(), po.getPlaceId()));
            String detail = String.format("評分:%s, 評論:%s\n地址:%s", po.getRating(), po.getUserRatingsTotal(), po.getVicinity());
            List<Action> actions = Arrays.asList(new URIAction("地圖", googleMapUrl, new URIAction.AltUri(googleMapUrl)));
            CarouselColumn carousel = new CarouselColumn(imageUrl, po.getName(), detail, actions);
            return carousel;
        }).collect(Collectors.toList());
        CarouselTemplate carouselTemplate = new CarouselTemplate(columns);
        return carouselTemplate;
    }

}
