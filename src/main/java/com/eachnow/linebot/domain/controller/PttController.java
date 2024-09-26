package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.PttInfoPO;
import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.gateway.PttApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/ptt")
public class PttController {
    private BeautyCrawlerService beautyCrawlerService;
    private PttApiService pttApiService;

    @Autowired
    public PttController(BeautyCrawlerService beautyCrawlerService,
                         PttApiService pttApiService) {
        this.beautyCrawlerService = beautyCrawlerService;
        this.pttApiService = pttApiService;
    }

    @GetMapping(value = "/beauty/url")
    public Result<String> getBeautyUrl() {
        Result<String> result = new Result<>();
        if (beautyCrawlerService.listPicture.size() == 0) {
            beautyCrawlerService.crawler(1);
            result.setCode(Result.NOT_FOUND);
            result.setData("找不到資料");
        } else {
            String pictureUrl = beautyCrawlerService.randomPicture().getPictureUrl();
            result.setData(pictureUrl);
        }
        return result;
    }

    @GetMapping(value = "/article")
    public Result<PttInfoPO> getPttArticle(@RequestParam(value = "board", defaultValue = "beauty") String board,
                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        PttInfoPO pttInfoPO = pttApiService.getPttInfoPO(board, size);
        Result<PttInfoPO> result = new Result<>();
        result.setData(pttInfoPO);
        return result;
    }

    @GetMapping(value = "/pictures")
    public Result<Set<String>> listPicture(@RequestParam(value = "url") String url) {
        Set<String> pictures = pttApiService.listPicture(url);
        Result<Set<String>> result = new Result<>();
        result.setData(pictures);
        return result;
    }

}
