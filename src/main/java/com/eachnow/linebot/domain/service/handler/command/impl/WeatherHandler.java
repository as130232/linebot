package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.openweather.ParameterPO;
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

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

//        Map<String, Map<String, ParameterPO>> dateAndElementMap = new HashMap<>();
//        weatherResultPO.getRecords().getLocation().get(0).getWeatherElement().forEach(weatherElementPO -> {
//            String element = weatherElementPO.getElementName();
//            weatherElementPO.getTime().forEach(timePO -> {
//                String date = timePO.getStartTime() + " - " + timePO.getEndTime();
//                Map<String, ParameterPO> elementMap = dateAndElementMap.get(date);
//                if (elementMap == null)
//                    elementMap = new HashMap<>();
//                elementMap.put(element, timePO.getParameter());
//                dateAndElementMap.put(date, elementMap);
//            });
//        });
        return new TextMessage(sb.toString());
    }

    private String parseUnit(String parameterUnit) {
        return parameterUnit.replace("百分比", "%");
    }

    private String parseDate(String time) {
        ZonedDateTime zonetime = ZonedDateTime.parse(time, DateUtils.yyyyMMddHHmmssDash);
        return zonetime.format(DateUtils.yyyyMMddHHmmDash);
    }


//    @PostConstruct
//    private void test() {
//        String text = "天氣 臺北市";
//        CommandPO commandPO = CommandPO.builder().userId("Uf52a57f7e6ba861c05be8837bfbcf0c6").text(text)
//                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
//        execute(commandPO);
//    }
}
