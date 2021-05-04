package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.fugle.FugleChartPO;
import com.eachnow.linebot.common.po.fugle.FugleDealtsPO;
import com.eachnow.linebot.common.po.fugle.FugleMetaPO;
import com.eachnow.linebot.common.po.fugle.FugleQuotePO;
import com.eachnow.linebot.domain.service.gateway.FugleApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FugleApiServiceImpl implements FugleApiService {
    @Value("${fugle.api.token}")
    private String FUGLE_API_KEY;
    private String FUGLE_URL = "https://api.fugle.tw/realtime/v0/intraday/";
    private RestTemplate restTemplate;

    @Autowired
    public FugleApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public FugleChartPO getChart(Integer symbolId) {
        try {
            String url = FUGLE_URL + "/chart?apiToken=" + FUGLE_API_KEY + "&symbolId=" + symbolId;
            ResponseEntity<FugleChartPO> responseEntity = restTemplate.getForEntity(url, FugleChartPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得fugle chart，失敗! symbolId:{}, error msg:{}", symbolId, e.getMessage());
        }
        return null;
    }

    @Override
    public FugleQuotePO getQuote(Integer symbolId) {
        try {
            String url = FUGLE_URL + "/quote?apiToken=" + FUGLE_API_KEY + "&symbolId=" + symbolId;
            ResponseEntity<FugleQuotePO> responseEntity = restTemplate.getForEntity(url, FugleQuotePO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得fugle quote，失敗! symbolId:{}, error msg:{}", symbolId, e.getMessage());
        }
        return null;
    }

    @Override
    public FugleMetaPO getMeta(Integer symbolId) {
        try {
            String url = FUGLE_URL + "/meta?apiToken=" + FUGLE_API_KEY + "&symbolId=" + symbolId;
            ResponseEntity<FugleMetaPO> responseEntity = restTemplate.getForEntity(url, FugleMetaPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得fugle meta，失敗! symbolId:{}, error msg:{}", symbolId, e.getMessage());
        }
        return null;
    }

    @Override
    public FugleDealtsPO getDealts(Integer symbolId) {
        try {
            String url = FUGLE_URL + "/dealts?apiToken=" + FUGLE_API_KEY + "&symbolId=" + symbolId;
            ResponseEntity<FugleDealtsPO> responseEntity = restTemplate.getForEntity(url, FugleDealtsPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得fugle dealts，失敗! symbolId:{}, error msg:{}", symbolId, e.getMessage());
        }
        return null;
    }
}
