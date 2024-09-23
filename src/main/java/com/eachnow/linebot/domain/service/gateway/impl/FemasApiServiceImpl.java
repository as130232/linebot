package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.femas.FemasPayResultPO;
import com.eachnow.linebot.common.po.femas.FemasPunchResultPO;
import com.eachnow.linebot.common.po.femas.request.FemasPayRecordIO;
import com.eachnow.linebot.common.po.femas.request.FemasPunchSearchDateIO;
import com.eachnow.linebot.domain.service.gateway.FemasApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

;

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
    public FemasPunchResultPO getPunchRecords(String token, String searchStart, String searchEnd) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            headers.set("Authorization", token);
            FemasPunchSearchDateIO searchDatePO = FemasPunchSearchDateIO.builder().type("user").searchStart(searchStart).searchEnd(searchEnd).offset(0).build();
            HttpEntity<FemasPunchSearchDateIO> httpEntity = new HttpEntity<>(searchDatePO, headers);
            String url = URL + "/photons/fsapi/V3/att_records.json";
            ResponseEntity<FemasPunchResultPO> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, FemasPunchResultPO.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("呼叫取得打卡記錄，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }


    @Override
    public FemasPayResultPO getPayrollRecords(String token, String yearMonth) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            headers.set("Authorization", token);
            FemasPayRecordIO io = FemasPayRecordIO.builder().yearMonth(yearMonth).build();
            HttpEntity<FemasPayRecordIO> httpEntity = new HttpEntity<>(io, headers);
            String url = URL + "/photons/fsapi/V3/payroll_records.json";
            ResponseEntity<FemasPayResultPO> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, FemasPayResultPO.class);
            FemasPayResultPO result = responseEntity.getBody();
            assert result != null;
            Integer total = result.getResponse().getDatas().stream().mapToInt(data -> Integer.parseInt(data.getReceived().replace(",", "")))
                    .sum();
            result.getResponse().setTotal(total);
            return result;
        } catch (Exception e) {
            log.error("呼叫取得當月薪資記錄，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }
}
