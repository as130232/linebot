package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DescriptionCommandPO;
import com.eachnow.linebot.common.po.DescriptionPO;
import com.eachnow.linebot.common.util.LineTemplateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.handler.location.LocationHandlerFactory;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Command({"place", "location", "地點", "地方"})
public class PlaceHandler implements CommandHandler {


    public static DescriptionPO getDescription(){
        List<DescriptionCommandPO> commands = new ArrayList<>();
        commands.add(DescriptionCommandPO.builder().explain("查詢地點").command("地點 {地點}").example("地點 景點").postback("地點 景點").build());
        return DescriptionPO.builder().title("地點").description("輸入地點並傳送當前位置，搜尋附近十家精選店家，並附上店家評價、住址、營業時間，可查詢:餐廳、景點、住宿、電影院、機場..等。")
                .commands(commands).imageUrl("https://marketingland.com/wp-content/ml-loads/2015/03/mobile-map-local-location-ss-1920-800x450.jpg").build();
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        GooglePlaceTypeEnum typeEnum = GooglePlaceTypeEnum.parse(ParamterUtils.getIndexOneParameter(text));
        if (typeEnum == null)
            return new TextMessage("Incorrect location. Please input again.\n找不到該地點請重新輸入.");
        LocationHandlerFactory.type = typeEnum;
        return LineTemplateUtils.getLocationButtonsTemplate(typeEnum.getName());
    }

}
