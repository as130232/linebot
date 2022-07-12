package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Command({"天氣"})
public class WeatherHandler implements CommandHandler {
    private OpenWeatherService openWeatherService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WeatherHandler(OpenWeatherService openWeatherService) {
        this.openWeatherService = openWeatherService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> params = commandPO.getParams();
        String locationName = ParamterUtils.getValueByIndex(params, 0).replace("台", "臺");
        if (locationName == null)
            locationName = "臺北市";
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
//        Map<String, Map<String, String>> weathersMap = new HashMap<>();
//        weatherResultPO.getRecords().getLocation().stream().forEach(locationPO -> {
//            List<WeatherElementPO> listElement = locationPO.getWeatherElement();
//            listElement.stream().forEach(weatherElementPO -> {
//                weatherElementPO.getTime().stream().forEach(timePO -> {
//                    String value = timePO.getParameter().getParameterName();
//                    //單位
//                    if (timePO.getParameter().getParameterUnit() != null)
//                        value += " " + parseUnit(timePO.getParameter().getParameterUnit());
//                    String time = parseDate(timePO.getStartTime()) + "-" + parseDate(timePO.getEndTime());
//                    String key = locationPO.getLocationName() + "_" + timePO.getStartTime();
//                    Map<String, String> weatherMap = weathersMap.get(key);
//                    if (weatherMap == null) {
//                        weatherMap = new HashMap<>();
//                        weatherMap.put("location", locationPO.getLocationName());
//                        weatherMap.put("time", time);
//                    }
//                    weatherMap.put(weatherElementPO.getElementName(), value);
//                    weathersMap.put(key, weatherMap);
//                });
//            });
//        });
//        List<WeatherPO> result = new ArrayList<>();
//        for (String key : weathersMap.keySet()) {
//            Map<String, String> weatherMap = weathersMap.get(key);
//            WeatherPO weatherPO = objectMapper.convertValue(weatherMap, WeatherPO.class);
//            result.add(weatherPO);
//        }
    }

    private String parseUnit(String parameterUnit) {
        return parameterUnit.replace("百分比", "%");
    }

    private String parseDate(String time) {
        ZonedDateTime zonetime = ZonedDateTime.parse(time, DateUtils.yyyyMMddHHmmssDash);
        return zonetime.format(DateUtils.yyyyMMddHHmmDash);
    }

//    @PostConstruct
    private void test() {
        String text = "天氣 臺北市";
        CommandPO commandPO = CommandPO.builder().userId("Uf52a57f7e6ba861c05be8837bfbcf0c6").text(text)
                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
        execute(commandPO);
    }
}
