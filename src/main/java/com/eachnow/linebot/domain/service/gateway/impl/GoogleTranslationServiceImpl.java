package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.google.GoogleDataTranslationPO;
import com.eachnow.linebot.common.po.google.GoogleTranslationPO;
import com.eachnow.linebot.domain.service.gateway.GoogleTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GoogleTranslationServiceImpl implements GoogleTranslationService {
    private static final String KEY = "AIzaSyBpCvliaopnpp3Oo9_0lL73wZLI8l1YV50";
    private RestTemplate restTemplate;

    @Autowired
    public GoogleTranslationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @PostConstruct
//    private void test() {
//        String text = "蘋果";
//        System.out.println(this.translate(text, LanguageEnum.KO.getLang()));
//    }

    @Override
    public String translate(String text, String code) {
        try {
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + KEY;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            GoogleTranslationPO googleTranslationPO = GoogleTranslationPO.builder().q(text).target(code).build();
            HttpEntity<GoogleTranslationPO> request = new HttpEntity<>(googleTranslationPO, headers);
            ResponseEntity<GoogleDataTranslationPO> responseEntity = restTemplate.postForEntity(url, request, GoogleDataTranslationPO.class);
            String result = responseEntity.getBody().getData().getTranslations().get(0).getTranslatedText();
            return result;
        } catch (Exception e) {
            log.error("呼叫Google翻譯API，失敗! error msg:{}", e.getMessage());
        }
        return "";
    }

}
