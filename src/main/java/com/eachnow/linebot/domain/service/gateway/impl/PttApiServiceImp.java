package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.po.PttInfoPO;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.domain.service.gateway.PttApiService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PttApiServiceImp implements PttApiService {
    private final String LINK = "https://disp.cc/b/";
    private RestTemplate restTemplate;

    @Autowired
    public PttApiServiceImp(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @PostConstruct
//    private void test() {
//        getPttInfoPO(PttEnum.GOSSIPING, 40);
//    }

    @Override
    public PttInfoPO getPttInfoPO(PttEnum pttEnum, int size) {
        String url = PttEnum.getUrlByDisp(pttEnum);
        Map<String, PttArticlePO> pttArticleMap = new HashMap<>(size);
        PttInfoPO pttInfoPO = null;
        while (pttArticleMap.size() < size) {
            pttInfoPO = getPttInfoPO(url);
            if (pttInfoPO == null)
                return null;

            for (PttArticlePO pttArticlePO : pttInfoPO.getArticles()) {
                if (pttArticleMap.size() == size)
                    continue;
                pttArticleMap.put(pttArticlePO.getWebUrl(), pttArticlePO);
            }
            url = pttInfoPO.getPageUpLink();
        }
        List<PttArticlePO> articles = pttArticleMap.keySet().stream().map(key -> pttArticleMap.get(key)).collect(Collectors.toList());
        pttInfoPO.setArticles(articles);
        return pttInfoPO;
    }

    private PttInfoPO getPttInfoPO(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//            headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36");
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            String result = responseEntity.getBody();
            Document doc = Jsoup.parse(result);
            Elements elements = doc.select("div[class~=row2]");
            List<PttArticlePO> list = new ArrayList<>(20);
            String name = doc.select("span[id~=board_div]").text();       //看版
            String boardPopularityStr = doc.select("span[class~=R0]").get(0).text();    //看板人氣:845 本日:102K 累積:987M
            Integer boardPopularity = Integer.valueOf(boardPopularityStr.split(" ")[0].replace("看板人氣:", ""));
            //取得上一頁連結
            String pageUpLink = LINK + doc.select("div[class~=topRight]").select("a").get(2).select("a").attr("href");
            //上一頁網址
            for (Element element : elements) {
                String title = element.select("span[class~=listTitle]").text();
                String popularityStr = element.select("span[class~=R0 bgB]").text();
                if (popularityStr.contains("/"))
                    popularityStr = popularityStr.split("/")[1];
                Integer popularity = parsePopularity(popularityStr);
                String link = LINK + element.select("span[class~=listTitle]").select("a").attr("href");
                String date = element.select("span[class~=L12]").attr("title");
                String author = element.select("span[class~=L18]").text().replace("(", "").replace(".)", "");
                list.add(PttArticlePO.builder().title(title).webUrl(link).author(author).date(date).popularity(popularity).build());
            }
            return PttInfoPO.builder().link(url).pageUpLink(pageUpLink).boardPopularity(boardPopularity).articles(list).build();
        } catch (Exception e) {
            log.error("呼叫取得PTT文章，失敗! url:{}, error msg:{}", url, e.getMessage());
        }
        return null;
    }

    private Integer parsePopularity(String popularityStr) {
        if (NumberUtils.isNumber(popularityStr)) {
            return Integer.valueOf(popularityStr);
        }
        Integer popularity = 0;
        if (popularityStr != null && popularityStr.toUpperCase(Locale.ROOT).contains("X")) {
            popularity = -100 * (Integer.valueOf(popularityStr.replace("X", "")));
        } else if (popularityStr != null && popularityStr.contains("爆")) {
            popularity = 1000;
        }
        return popularity;
    }
}
