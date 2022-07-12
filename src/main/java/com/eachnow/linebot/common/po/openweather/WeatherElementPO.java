package com.eachnow.linebot.common.po.openweather;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WeatherElementPO {
    private String elementName;
    private List<TimePO> time;
}
