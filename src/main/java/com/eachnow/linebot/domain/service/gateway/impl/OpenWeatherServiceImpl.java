package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class OpenWeatherServiceImpl implements OpenWeatherService {
    @Value("${open.weather.auth.code}")
    private String AUTH_CODE;
    private RestTemplate restTemplate;

    @Autowired
    public OpenWeatherServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void test() {
        String area = "台北市";
        WeatherResultPO weatherResultPO = this.getWeatherInfo(area);
        System.out.println(weatherResultPO);
    }

    public WeatherResultPO getWeatherInfo(String locationName) {
        String url = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization={authCode}&locationName={locationName}"
                .replace("{locationName}", locationName).replace("{authCode}", AUTH_CODE);
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
