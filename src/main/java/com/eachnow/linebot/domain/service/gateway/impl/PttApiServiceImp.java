package com.eachnow.linebot.domain.service.gateway.impl;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.po.PttInfoPO;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.domain.service.gateway.PttApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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
    private final String DOMAIN = "https://disp.cc";
    private final String LINK = DOMAIN + "/b/";
    private final RestTemplate restTemplate;

    @Autowired
    public PttApiServiceImp(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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
        List<PttArticlePO> articles = pttArticleMap.keySet().stream().map(pttArticleMap::get)
                .sorted(Comparator.comparing(PttArticlePO::getPopularity).reversed())
                .collect(Collectors.toList());
        //根據熱門排序
        assert pttInfoPO != null;
        pttInfoPO.setArticles(articles);
        pttInfoPO.setPttEnum(pttEnum);
        return pttInfoPO;
    }

    /**
     * 取得該文章內對應圖片列表
     */
    @Override
    public Set<String> listPicture(String url) {
        Set<String> result = new HashSet<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        String response = responseEntity.getBody();
        try {
            assert response != null;
            Document doc = Jsoup.parse(response);
            Elements textCssDiv = doc.select("div[class~=text_css]");
            Elements imgDivs = textCssDiv.select("div.img");
            for (Element imgDiv : imgDivs) {
                Element img = imgDiv.selectFirst("img[data-src]");
                if (img != null) {
                    String pictureUrl = img.attr("data-src");
                    result.add(pictureUrl);
                }
            }
        } catch (Exception e) {
            log.error("呼叫取得PTT文章，失敗! url:{}, error msg:{}", url, e.getMessage());
        }
        return result;
    }

    /**
     * 取得該版(八卦、表特..)文章列表
     */
    private PttInfoPO getPttInfoPO(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        String response = responseEntity.getBody();
        try {
            assert response != null;
            Document doc = Jsoup.parse(response);
            List<PttArticlePO> list = new ArrayList<>(20);
            int boardPopularity = 0;
            if (doc.select("div[class~=topRight]").text().contains("進入看板")) {
                return PttInfoPO.builder().link(url).pageUpLink(LINK + doc.select("div[class~=topRight]").select("a").attr("href")).boardPopularity(boardPopularity).articles(list).build();
            }
            Elements elements = doc.select("div[class~=row2]");
            String name = doc.select("span[id~=board_div]").text();       //看版
            if (doc.select("span[class~=R0]").size() > 0) {
                String boardPopularityStr = doc.select("span[class~=R0]").get(0).text();    //看板人氣:845 本日:102K 累積:987M
                boardPopularity = Integer.parseInt(boardPopularityStr.split(" ")[0].replace("看板人氣:", ""));
            }
            //取得上一頁連結
            String pageUpLink = LINK + doc.select("div[class~=topRight]").select("a").get(2).select("a").attr("href");
            //上一頁網址
            for (Element element : elements) {
                String title = element.select("span[class~=listTitle]").text().replace("■ ", "");
                if (title.contains("[公告]") || title.contains("[刪除]") || title.contains("···"))
                    continue;
                Integer popularity = 0;
                Elements popularityElements = element.select("span[class~=fg]");
                for (Element popularityElement : popularityElements) {
                    String popularityTitle = popularityElement.attr("title");
                    if (!Strings.isEmpty(popularityTitle) && popularityTitle.contains("累積人氣:")) {
                        String popularityStr = popularityTitle.replace("累積人氣:", "");
                        popularity = parsePopularity(popularityStr);
                        break;
                    }
                }
                String link = DOMAIN + element.select("span[class~=listTitle]").select("a").attr("href");
                String date = element.select("span[class~=L12]").attr("title");
                String author = element.select("span[class~=L18]").text().replace("(", "").replace(".)", "");
                list.add(PttArticlePO.builder().title(title).webUrl(link).author(author).date(date).popularity(popularity).build());
            }
            return PttInfoPO.builder().link(url).pageUpLink(pageUpLink).boardPopularity(boardPopularity).articles(list).build();
        } catch (Exception e) {
            log.error("呼叫取得PTT該版，失敗! url:{}, error msg:{}", url, e.getMessage());
        }
        return null;
    }

    private Integer parsePopularity(String popularityStr) {
        popularityStr = popularityStr.trim();
        if (NumberUtils.isNumber(popularityStr)) {
            return Integer.valueOf(popularityStr);
        }
        int popularity = 0;
        if (popularityStr.toUpperCase(Locale.ROOT).contains("X")) {
            popularity = -100 * (Integer.parseInt(popularityStr.replace("X", "")));
        } else if (popularityStr.contains("爆")) {
            popularity = 1000;
        }
        return popularity;
    }

}
