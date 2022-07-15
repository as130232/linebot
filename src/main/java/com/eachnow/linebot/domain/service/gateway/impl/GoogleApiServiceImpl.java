package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.po.google.map.ResultLocationPO;
import com.eachnow.linebot.common.po.google.translation.InputTranslationPO;
import com.eachnow.linebot.common.po.google.translation.OutputTranslationPO;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Component
public class GoogleApiServiceImpl implements GoogleApiService {
    @Value("${google.api.key}")
    private String GOOGLE_API_KEY;
    private final String BASE_URL = "https://translation.googleapis.com/language/translate/v2";
    private RestTemplate restTemplate;

    @Autowired
    public GoogleApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @PostConstruct
//    private void test() {
//        String text = "蘋果";
//        System.out.println(this.translate(text, LanguageEnum.EN.getLang()));
//    }

    @Override
    public String translate(String text, String lang) {
        if (Strings.isEmpty(text) || Strings.isEmpty(lang))
            return "";
        try {
            String url = BASE_URL + "?key=" + GOOGLE_API_KEY;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            InputTranslationPO inputTranslationPO = InputTranslationPO.builder().q(text).target(lang).build();
            HttpEntity<InputTranslationPO> request = new HttpEntity<>(inputTranslationPO, headers);
            ResponseEntity<OutputTranslationPO> responseEntity = restTemplate.postForEntity(url, request, OutputTranslationPO.class);
            String result = responseEntity.getBody().getData().getTranslations().get(0).getTranslatedText();
            return result;
        } catch (Exception e) {
            log.error("呼叫Google翻譯API，失敗! error msg:{}", e.getMessage());
        }
        return "";
    }

    @Override
    public ResultLocationPO getLocation(String latitude, String longitude, String searchWord, String language) {
        if (language == null)
            language = LanguageEnum.TW.getLang();   //default
        GooglePlaceTypeEnum typeEnum = GooglePlaceTypeEnum.parse(searchWord);
        try {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "key={key}".replace("{key}", GOOGLE_API_KEY) +
                    "&location={latitude},{longitude}".replace("{latitude}", latitude).replace("{longitude}", longitude) +
                    "&rankby=distance" +
                    "&language={language}".replace("{language}", language);
            if (Objects.nonNull(typeEnum)) {
                url += "&type={type}".replace("{type}", typeEnum.toString().toLowerCase());
            } else {
                url += "&keyword=" + searchWord;
            }
            log.info("google getLocation url:{}", url);
            ResponseEntity<ResultLocationPO> responseEntity = restTemplate.getForEntity(url, ResultLocationPO.class);
            ResultLocationPO result = responseEntity.getBody();
            log.info("google getLocation result:{}", JsonUtils.toJsonString(result));
            return result;
        } catch (Exception e) {
            log.error("呼叫Google Map API，失敗! error msg:{}", e.getMessage());
        }
        return null;
    }

    @Override
    public String parseMapUrl(String lat, String lng, String placeId) {
        return "https://www.google.com/maps/search/?api=1&query={lat},{lng}&query_place_id={placeId}"
                .replace("{lat}", lat).replace("{lng}", lng).replace("{placeId}", placeId);
    }

    @Override
    public String parseMapPictureUrl(String photoreference) {
        return "https://maps.googleapis.com/maps/api/place/photo?key={key}&photoreference={photoreference}&maxwidth=1024"
                .replace("{key}", GOOGLE_API_KEY).replace("{photoreference}", photoreference);
    }
}
