package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.annotation.Description;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Description("天氣 {地區} {類型}")
@Command({"天氣"})
public class WeatherHandler implements CommandHandler {
    private OpenWeatherService openWeatherService;

    @Autowired
    public WeatherHandler(OpenWeatherService openWeatherService) {
        this.openWeatherService = openWeatherService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> params = commandPO.getParams();
        String locationName = ParamterUtils.getValueByIndex(params, 0).replace("台", "臺");
        String elementName = WeatherElementEnum.getElement(ParamterUtils.getValueByIndex(params, 1));
        WeatherResultPO weatherResultPO = openWeatherService.getWeatherInfo(locationName, elementName);
        if (weatherResultPO.getRecords().getLocation().size() == 0)
            return new TextMessage("查無此地:" + locationName);
        StringBuilder sb = new StringBuilder();
        sb.append(" - " + weatherResultPO.getRecords().getDatasetDescription() + " - ");
        sb.append("\n");
        weatherResultPO.getRecords().getLocation().stream().forEach(locationPO -> {
            sb.append("＊" + locationPO.getLocationName());
            sb.append("\n");
            List<WeatherElementPO> listElement = locationPO.getWeatherElement();
            listElement.stream().forEach(weatherElementPO -> {
                sb.append("　【" + WeatherElementEnum.getName(weatherElementPO.getElementName()) + "】");
                sb.append("\n");
                weatherElementPO.getTime().stream().forEach(timePO -> {
                    sb.append(parseDate(timePO.getStartTime()) + "-" + parseDate(timePO.getEndTime()));
                    sb.append("　" + timePO.getParameter().getParameterName());
                    if (timePO.getParameter().getParameterUnit() != null) {  //單位
                        sb.append("({unit})".replace("{unit}", parseUnit(timePO.getParameter().getParameterUnit())));
                    }
                    sb.append("\n");
                });
                sb.append("\n");
            });
        });
        return new TextMessage(sb.toString());
    }

    private String parseUnit(String parameterUnit) {
        return parameterUnit.replace("百分比", "%");
    }

    private String parseDate(String time) {
        ZonedDateTime zonetime = ZonedDateTime.parse(time, DateUtils.yyyyMMddHHmmssDash);
        return zonetime.format(DateUtils.yyyyMMddHHmmDash);
    }

}
