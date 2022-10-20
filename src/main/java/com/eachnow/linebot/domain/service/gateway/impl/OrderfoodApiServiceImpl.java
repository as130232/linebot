package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.domain.service.gateway.OrderfoodApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OrderfoodApiServiceImpl implements OrderfoodApiService {
    private final String URL = "https://orderfood0901.herokuapp.com/";
    private RestTemplate restTemplate;

    @Autowired
    public OrderfoodApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void preventDormancy() {
        try {
            String url = URL + "/test/preventDormancy";
            restTemplate.getForEntity(url, Void.class);
        } catch (Exception e) {
            log.error("call api preventDormancy，failed! error msg:{}", e.getMessage());
        }
    }
}
