package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.femas.FemasResultPO;
import com.eachnow.linebot.common.po.femas.request.FemasSearchDatePO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.gateway.FemasApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class FemasApiServiceImpl implements FemasApiService {
    @Value("${femas.token}")
    private String FEMAS_TOKEN;
    private final String URL = "https://femashr-app-api.femascloud.com";

    private RestTemplate restTemplate;

    @Autowired
    public FemasApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public FemasResultPO getRecords(String token, String searchStart, String searchEnd) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            headers.set("Authorization", token);
            FemasSearchDatePO searchDatePO = FemasSearchDatePO.builder().type("user").searchStart(searchStart).searchEnd(searchEnd).offset(0).build();
            HttpEntity<FemasSearchDatePO> httpEntity = new HttpEntity<>(searchDatePO, headers);
            String url = URL + "/photons/fsapi/V3/att_records.json";
            ResponseEntity<FemasResultPO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, FemasResultPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得當周打卡記錄，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }
}
