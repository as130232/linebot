package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.po.PttInfoPO;
import com.eachnow.linebot.domain.service.gateway.PttApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 爬取資源服務
 */
@Slf4j
@Component
public class BeautyCrawlerService {
    private final ThreadPoolExecutor pttCrawlerExecutor;
    private final PttCrawlerService pttCrawlerService;
    private final PttApiService pttApiService;

    public final Integer MAX_SIZE = 500;
    public List<PttArticlePO> listArticle = new ArrayList<>(MAX_SIZE);
    public Set<String> pictures = new HashSet<>(MAX_SIZE);

    @Autowired
    public BeautyCrawlerService(@Qualifier("ptt-crawler-executor") ThreadPoolExecutor pttCrawlerExecutor,
                                PttCrawlerService pttCrawlerService, PttApiService pttApiService) {
        this.pttCrawlerExecutor = pttCrawlerExecutor;
        this.pttCrawlerService = pttCrawlerService;
        this.pttApiService = pttApiService;
    }

//    @PostConstruct
//    public void init() {
//        crawler(10);
//        String pic = randomPicture();
//        log.info(pic);
//    }

    /**
     * 容易造成heroku oom將改用api取得對應圖片
     */
//    public void crawler(int pageSize) {
//        CompletableFuture.runAsync(() -> {
//            List<PttArticlePO> result = pttCrawlerService.crawler(PttEnum.BEAUTY, pageSize, PttEnum.TYPE_PICTURE);
//            this.setPicture(result);
//        }, pttCrawlerExecutor).exceptionally(e -> {
//                    log.error("爬取PTT-表特版，失敗! error msg:{}", e.getMessage());
//                    return null;
//                }
//        );
//    }

    /**
     * 根據api爬取表特版圖片
     */
    public void crawler(int size) {
        PttInfoPO pttInfoPO = pttApiService.getPttInfoPO(PttEnum.BEAUTY.getValue(), size);
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

    private void setPttArticles(List<PttArticlePO> listPictureOnPage) {
        listArticle.addAll(listPictureOnPage);
        if (listArticle.size() > MAX_SIZE) {
            int i = 0;
            while (listArticle.size() > MAX_SIZE) {
                listArticle.remove(i);
                i++;
            }
        }
    }

    /**
     * 取得隨機一篇文章
     */
    public PttArticlePO randomArticle() {
        int item = new Random().nextInt(listArticle.size());
        return listArticle.get(item);
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

}
