package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.opendata.CpcOilPricePO;
import com.eachnow.linebot.domain.service.gateway.CpcOilApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 中油API服務
 */
@Slf4j
@Component
public class CpcOilApiServiceImpl implements CpcOilApiService {
    private final String CPC_URL = "https://www.cpc.com.tw";
    private RestTemplate restTemplate;

    @Autowired
    public CpcOilApiServiceImpl(@Qualifier("converter-resttemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public CpcOilPricePO getOilPrice() {
        try {
            String url = CPC_URL + "/GetOilPriceJson.aspx?type=TodayOilPriceString";
            ResponseEntity<CpcOilPricePO> responseEntity = restTemplate.getForEntity(url, CpcOilPricePO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得中油及時油價，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }

    @Override
    public CpcOilPricePO getCurrency() {
        try {
            String url = CPC_URL + "/GetOilPriceJson.aspx?type=TargetOilPrice";
            ResponseEntity<CpcOilPricePO> responseEntity = restTemplate.getForEntity(url, CpcOilPricePO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得中油匯率，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }
}
