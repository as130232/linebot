package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.openweather.TimePO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.domain.service.gateway.WeatherApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class WeatherApiServiceImpl implements WeatherApiService {
    @Value("${open.weather.auth.code}")
    private String AUTH_CODE;
    private final String BASE_URL = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/F-C0032-001";
    private final RestTemplate restTemplate;


    @Autowired
    public WeatherApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public WeatherResultPO getWeatherInfo(String locationName, String elementName) {
        String url = BASE_URL + "?Authorization=" + AUTH_CODE;
        if (locationName != null)
            url = url + "&locationName=" + locationName;
        if (elementName != null)
            url = url + "&elementName=" + elementName;
        try {
            ResponseEntity<WeatherResultPO> responseEntity = restTemplate.getForEntity(url, WeatherResultPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("取得天氣資訊，失敗! url:{}, error msg:{}", url, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isRain(WeatherResultPO po) {
        if (po == null) {
            return false;
        }
        for (WeatherElementPO weatherElementPO : po.getRecords().getLocation().get(0).getWeatherElement()) {
            for (TimePO timePO : weatherElementPO.getTime()) {
                int unit = Integer.parseInt(timePO.getParameter().getParameterName());
                if (unit >= 70) {
                    return true;
                }
            }
        }
        return false;
    }
}
