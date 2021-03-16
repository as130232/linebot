package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Command({"天氣"})
public class WeatherHandler implements CommandHandler {
    private OpenWeatherService openWeatherService;
    public static final ZoneId CST_ZONE_ID = ZoneId.of("Asia/Taipei");
    public static final DateTimeFormatter yyyyMMddHHmmssDash = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmDash = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(CST_ZONE_ID);

    @Autowired
    public WeatherHandler(OpenWeatherService openWeatherService) {
        this.openWeatherService = openWeatherService;
    }

    @PostConstruct
    private void test() {
        String area = "天氣 新北市";
        this.execute(area);
    }

    @Override
    public Message execute(String parameters) {
        parameters = parameters.replace("台", "臺");
        List<String> params = ParamterUtils.parse(parameters);
        String locationName = params.get(1);
        String elementName = params.size() > 2 ? WeatherElementEnum.getElement(params.get(0)) : null;
        WeatherResultPO weatherResultPO = openWeatherService.getWeatherInfo(locationName, elementName);

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

    public String getFormat() {
        return "天氣 {地區} {類型}";
    }

    private String parseUnit(String parameterUnit) {
        return parameterUnit.replace("百分比", "%");
    }

    private String parseDate(String time) {
        ZonedDateTime zonetime = ZonedDateTime.parse(time, yyyyMMddHHmmssDash);
        return zonetime.format(yyyyMMddHHmmDash);
    }

}
