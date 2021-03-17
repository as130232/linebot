package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.po.google.translation.OutputTranslationPO;
import com.eachnow.linebot.common.po.google.translation.InputTranslationPO;
import com.eachnow.linebot.domain.service.gateway.GoogleTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GoogleTranslationServiceImpl implements GoogleTranslationService {
    @Value("${google.api.key}")
    private String GOOGLE_API_KEY;
    private RestTemplate restTemplate;

    @Autowired
    public GoogleTranslationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @PostConstruct
//    private void test() {
//        String text = "蘋果";
//        System.out.println(this.translate(text, LanguageEnum.EN.getLang()));
//    }

    @Override
    public String translate(String text, String code) {
        try {
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + GOOGLE_API_KEY;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            InputTranslationPO inputTranslationPO = InputTranslationPO.builder().q(text).target(code).build();
            HttpEntity<InputTranslationPO> request = new HttpEntity<>(inputTranslationPO, headers);
            ResponseEntity<OutputTranslationPO> responseEntity = restTemplate.postForEntity(url, request, OutputTranslationPO.class);
            String result = responseEntity.getBody().getData().getTranslations().get(0).getTranslatedText();
            return result;
        } catch (Exception e) {
            log.error("呼叫Google翻譯API，失敗! error msg:{}", e.getMessage());
        }
        return "";
    }

}
