package com.eachnow.linebot.domain.service.crawler;

import com.eachnow.linebot.common.constant.PttConstant;
import com.eachnow.linebot.common.po.PttArticlePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 爬取資源服務
 */
@Slf4j
@Component
public class ActressCrawlerService {

    private final ThreadPoolExecutor pttCrawlerExecutor;
    private PttCrawlerService pttCrawlerService;

    public final Integer MAX_SIZE = 500;
    public List<String> listPicture = new ArrayList<>(MAX_SIZE);

    @Autowired
    public ActressCrawlerService(@Qualifier("ptt-crawler-executor") ThreadPoolExecutor pttCrawlerExecutor,
                                 PttCrawlerService pttCrawlerService) {
        this.pttCrawlerService = pttCrawlerService;
        this.pttCrawlerExecutor = pttCrawlerExecutor;
    }

    //    @PostConstruct
    public void init() {
        listPicture = new ArrayList<>(MAX_SIZE);
        crawler(2);
    }

    public void crawler(int pageSize) {
        String url = PttConstant.AVGIRLS_URL;
        CompletableFuture.runAsync(() -> {
            List<String> result = pttCrawlerService.crawler(url, pageSize, PttConstant.TYPE_PICTURE).stream().map(PttArticlePO::getPictureUrl).collect(Collectors.toList());
            this.setPicture(result);
        }, pttCrawlerExecutor).exceptionally(e -> {
                    log.error("爬取PTT版，失敗! url:{}, error msg:{}", url, e.getMessage());
                    return null;
                }
        );
    }

    private void setPicture(List<String> listPictureOnPage) {
        listPicture.addAll(listPictureOnPage);
        if (listPicture.size() > MAX_SIZE) {
            int i = 0;
            while (listPicture.size() > MAX_SIZE) {
                listPicture.remove(i);
                i++;
            }
        }
    }

    public String randomPicture() {
        int item = new Random().nextInt(listPicture.size());
        return listPicture.get(item);
    }
}
