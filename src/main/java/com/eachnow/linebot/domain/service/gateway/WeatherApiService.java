package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.openweather.WeatherResultPO;

public interface WeatherApiService {
    /**
     * 取得天氣資訊
     */
    public WeatherResultPO getWeatherInfo(String locationName, String elementName);

    public boolean isRain(WeatherResultPO po);
}
