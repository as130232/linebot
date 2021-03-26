package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.domain.service.gateway.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 匯率API服務
 */
@Slf4j
@Component
public class CurrencyServiceImpl implements CurrencyService {
    private RestTemplate restTemplate;
    private final String BASE_URL = "https://tw.rter.info/capi.php";
    public JSONObject currentDayCurrency = new JSONObject();

    @Autowired
    public CurrencyServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void init() {
        currentDayCurrency = this.getCurrency();
    }

    @Override
    public JSONObject getCurrency() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            HttpEntity httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(BASE_URL, HttpMethod.GET, httpEntity, String.class);
            JSONObject json = new JSONObject(responseEntity.getBody());
            return json;
        } catch (Exception e) {
            log.error("取得即時匯率資訊，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }

}
