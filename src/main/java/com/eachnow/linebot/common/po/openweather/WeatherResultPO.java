package com.eachnow.linebot.common.po.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResultPO {
    private String success;
    private ResultPO result;
    private RecordPO records;
}
