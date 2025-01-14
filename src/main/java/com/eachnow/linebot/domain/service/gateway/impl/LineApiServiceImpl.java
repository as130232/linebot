package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.line.LineNotifyTokenPO;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.gateway.LineApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Component
public class LineApiServiceImpl implements LineApiService {
    private RestTemplate restTemplate;
    private LineConfig lineConfig;

    @Autowired
    public LineApiServiceImpl(RestTemplate restTemplate,
                              LineConfig lineConfig) {
        this.restTemplate = restTemplate;
        this.lineConfig = lineConfig;
    }

    @Override
    public String getLineNotifyToken(String code) {
        try {
            String url = "https://notify-bot.line.me/oauth/token?grant_type=authorization_code&redirect_uri=https://linebotmuyu.herokuapp.com/linebot/notify/subscribe" +
                    "&client_id={clientId}&client_secret={clientSecret}&code={code}".replace("{clientId}", lineConfig.getLineNotifyClientId())
                            .replace("{clientSecret}", lineConfig.getLineNotifyClientSecret()).replace("{code}", code);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<LineNotifyTokenPO> responseEntity = restTemplate.postForEntity(url, request, LineNotifyTokenPO.class);
            String result = Objects.requireNonNull(responseEntity.getBody()).getAccessToken();
            log.info("取得line notify token 成功。token:{}", result);
            return result;
        } catch (Exception e) {
            log.error("呼叫取得line notify token，失敗! code:{}, error msg:{}", code, e.getMessage());
        }
        return null;
    }
}
