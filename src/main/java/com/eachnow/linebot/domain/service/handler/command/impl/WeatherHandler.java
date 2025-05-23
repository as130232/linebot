package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.FlexMessageUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.WeatherApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Command({"天氣"})
public class WeatherHandler implements CommandHandler {
    private WeatherApiService weatherApiService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WeatherHandler(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> params = commandPO.getParams();
        String locationName = ParamterUtils.getValueByIndex(params, 0);
        if (Objects.isNull(locationName))
            locationName = "臺北市";
        locationName = locationName.replace("台", "臺");
        String elementName = WeatherElementEnum.getElement(ParamterUtils.getValueByIndex(params, 1));
        WeatherResultPO weatherResultPO = weatherApiService.getWeatherInfo(locationName, elementName);
        if (weatherResultPO.getRecords().getLocation().size() == 0)
            return new TextMessage("查無此地:" + locationName);
//        return new TextMessage(getWeatherInfo(weatherResultPO));
        return FlexMessageUtils.getWeatherCard(weatherResultPO);
    }

    private String parseUnit(String parameterUnit) {
        return parameterUnit.replace("百分比", "%");
    }

    private String getWeatherInfo(WeatherResultPO weatherResultPO) {
        StringBuilder sb = new StringBuilder();
        sb.append(" - " + weatherResultPO.getRecords().getDatasetDescription() + " - ");
        sb.append("\n");
        weatherResultPO.getRecords().getLocation().forEach(locationPO -> {
            sb.append("＊" + locationPO.getLocationName());
            sb.append("\n");
            List<WeatherElementPO> listElement = locationPO.getWeatherElement();
            listElement.forEach(weatherElementPO -> {
                sb.append("　【" + WeatherElementEnum.getName(weatherElementPO.getElementName()) + "】");
                sb.append("\n");
                weatherElementPO.getTime().forEach(timePO -> {
                    sb.append(DateUtils.parseDateTime(timePO.getStartTime()) + "-" + DateUtils.parseDateTime(timePO.getEndTime()));
                    sb.append("　" + timePO.getParameter().getParameterName());
                    if (timePO.getParameter().getParameterUnit() != null) {  //單位
                        sb.append("({unit})".replace("{unit}", parseUnit(timePO.getParameter().getParameterUnit())));
                    }
                    sb.append("\n");
                });
                sb.append("\n");
            });
        });
        return sb.toString();
    }
}
