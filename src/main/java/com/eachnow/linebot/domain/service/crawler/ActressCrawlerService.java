package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.po.PttInfoPO;
import com.eachnow.linebot.domain.service.gateway.PttApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 爬取資源服務
 */
@Slf4j
@Component
public class ActressCrawlerService {

    private final PttApiService pttApiService;
    public final Integer MAX_SIZE = 500;
    public Map<String, PttArticlePO> articleMap = new HashMap<>(MAX_SIZE);
    public Set<String> pictures = new HashSet<>(MAX_SIZE);

    @Autowired
    public ActressCrawlerService(PttApiService pttApiService) {
        this.pttApiService = pttApiService;
    }

    /**
     * 根據api爬取表特版圖片
     */
    public void crawler(int size) {
        PttInfoPO pttInfoPO = pttApiService.getPttInfoPO(PttEnum.JAPANAVGIRLS.getValue(), size);
        if (pttInfoPO == null) {
            return;
        }
        this.setPttArticles(pttInfoPO.getArticles());
        for (PttArticlePO article : pttInfoPO.getArticles()) {
            //取得對應文章中所有圖片列表
            Set<String> listPicture = pttApiService.listPicture(article.getWebUrl());
            pictures.addAll(listPicture);
        }
        log.info("crawler ptt beauty success. pictures size:{}", pictures.size());
    }

    private void setPttArticles(List<PttArticlePO> listPicture) {
        for (PttArticlePO po : listPicture) {
            articleMap.put(po.getWebUrl(), po);
        }
        if (articleMap.size() > MAX_SIZE) {
            int i = 0;
            while (articleMap.size() > MAX_SIZE) {
                articleMap.remove(i);
                i++;
            }
        }
    }

    public static PttArticlePO getRandomValue(Map<String, PttArticlePO> map) {
        // 將 HashMap 的值轉換為數組
        PttArticlePO[] values = map.values().toArray(new PttArticlePO[0]);
        // 使用 Random 類生成隨機索引
        Random random = new Random();
        int randomIndex = random.nextInt(values.length);
        // 返回隨機選擇的值
        return values[randomIndex];
    }

    /**
     * 取得隨機一篇文章
     */
    public PttArticlePO randomArticle() {
        return getRandomValue(articleMap);
    }

    /**
     * 取得隨機一張圖片
     */
    public String randomPicture() {
        if (pictures.size() == 0) {
            return "";
        }
        String[] array = new String[pictures.size()];
        pictures.toArray(array);
        // 使用 Random 類生成隨機索引
        Random random = new Random();
        int randomIndex = random.nextInt(array.length);
        // 返回隨機選擇的元素
        return array[randomIndex];
    }

    public Set<PttArticlePO> listArticle(int size) {
        if (size > articleMap.size()) {
            crawler(size);
        }
        Set<PttArticlePO> result = new HashSet<>(size);
        while (result.size() != size && articleMap.size() >= size)
            result.add(randomArticle());
        return result;
    }
}
