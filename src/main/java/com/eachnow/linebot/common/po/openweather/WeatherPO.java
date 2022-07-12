package com.eachnow.linebot.common.po.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherPO {
    private String location;
    private String time;
    @JsonProperty(value = "Wx")
    private String wx;      //氣溫
    @JsonProperty(value = "MaxT")
    private String maxT;    //最高溫度
    @JsonProperty(value = "MinT")
    private String minT;    //最低溫度
    @JsonProperty(value = "PoP")
    private String pop;     //降雨機率
    @JsonProperty(value = "CI")
    private String ci;      //舒適度
}
