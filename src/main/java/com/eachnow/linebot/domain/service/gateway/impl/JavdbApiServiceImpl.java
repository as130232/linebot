package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.javdb.NetflavDataPO;
import com.eachnow.linebot.domain.service.gateway.JavdbApiService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class JavdbApiServiceImpl implements JavdbApiService {
    private String JAVDB_URL = "https://javdb.com";
    private String NETFLAV_URL = "https://netflav.com";
    private RestTemplate restTemplate;

    @Autowired
    public JavdbApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @PostConstruct
//    private void test() {
//        this.getLeaderboard("daily");
//        this.search("cawd-259");
//    }

    @Override
    public NetflavDataPO search(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("User-Agent", "PostmanRuntime/7.28.4");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = NETFLAV_URL + "/api98/video/advanceSearchVideo?type=title&page=1&keyword=" + code;
        try {
            ResponseEntity<NetflavDataPO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, NetflavDataPO.class);
            NetflavDataPO dataPO = responseEntity.getBody();
            return dataPO;
        } catch (Exception e) {
            log.error("搜尋該資源，失敗! code:{}, error msg:{}", code, e.getMessage());
        }
        return null;
    }

    private void getLeaderboard(String period) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("User-Agent", "PostmanRuntime/7.28.4");
        headers.set("Cookie", "_jdb_session=kOdJlQr5%2BxoA8Yed7u1GLyzEDgZmtuIDkKbay7%2FX%2FArM%2BfeK683wtq31eTkROzJ7nlUgDM%2BixPBbhhYkiUjkqk7DlcS1%2F7lLMTEfOxNVKQZ%2FtsuBVfWxHwkS5lIzoyGgMaIc9XmNSzHZ%2BJ2SCgjyYxFkqW1%2BLC1TFv5lJurUcYAeEjm0Q%2FTUUZEbDuoyPeM1xbEaPdEBnTebbokXdfMVAOxUB8gGOTgSOrh7YuNaUf%2Fmlkat3r5IWVeFDd%2BoCzgcmqetHUO74qDFLCEJARqV1e7LAl6%2BWOjZ2FWSO0ZClX7XdjXOdvvHl%2F6x--4mKY%2BjbU9P5QvQj%2B--w56zvYekrOKgNm1QGZ0o6g%3D%3D; locale=zh; theme=auto");
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Connection", "keep-alive");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = JAVDB_URL + "/rankings/video_censored?period=" + period;
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            String response = responseEntity.getBody();
            Document doc = Jsoup.parse(response);
            System.out.println();
        } catch (Exception e) {
            log.error("呼叫取得Javdb排行榜，失敗! period:{}, error msg:{}", period, e.getMessage());
        }
    }


}
