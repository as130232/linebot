package com.eachnow.linebot.domain.service.gateway.impl;

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
            WeatherResultPO weatherResultPO = responseEntity.getBody();
            return weatherResultPO;
        } catch (Exception e) {
            log.error("取得天氣資訊，失敗! url:{}, error msg:{}", url, e.getMessage());
        }
        return null;
    }
}
