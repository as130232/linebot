package com.eachnow.linebot.domain.service.schedule;

import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.openweather.TimePO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.crawler.JavdbCrawlerService;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import com.eachnow.linebot.domain.service.gateway.OrderfoodApiService;
import com.eachnow.linebot.domain.service.gateway.TwseApiService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 排程服務
 */
//@EnableScheduling
@Slf4j
@Component
public class ScheduledService {
    @Value("${cron.flag:false}")
    private boolean CRON_EXECUTE;
    private final ThreadPoolExecutor crawlerExecutor;
    private LineConfig lineConfig;
    private LineNotifySender lineNotifySender;
    private BeautyCrawlerService beautyCrawlerService;
    private OpenWeatherService openWeatherService;
    private TwseApiService twseApiService;
    private JavdbCrawlerService javdbCrawlerService;
    private OrderfoodApiService orderfoodApiService;

    @Autowired
    public ScheduledService(@Qualifier("ptt-crawler-executor") ThreadPoolExecutor crawlerExecutor,
                            LineConfig lineConfig,
                            LineNotifySender lineNotifySender,
                            BeautyCrawlerService beautyCrawlerService,
                            OpenWeatherService openWeatherService,
                            TwseApiService twseApiService,
                            JavdbCrawlerService javdbCrawlerService,
                            OrderfoodApiService orderfoodApiService) {
        this.crawlerExecutor = crawlerExecutor;
        this.lineConfig = lineConfig;
        this.lineNotifySender = lineNotifySender;
        this.beautyCrawlerService = beautyCrawlerService;
        this.openWeatherService = openWeatherService;
        this.twseApiService = twseApiService;
        this.javdbCrawlerService = javdbCrawlerService;
        this.orderfoodApiService = orderfoodApiService;
    }

    public void switchCron(boolean isOpen) {
        CRON_EXECUTE = isOpen;
    }

    public boolean getCron() {
        return CRON_EXECUTE;
    }

    @Scheduled(cron = "0 0 8,14,20 * * ?")
    public void beautyCrawler() {
        if (!CRON_EXECUTE)
            return;
        beautyCrawlerService.crawler(2);
        log.info("[schedule] 爬取表特版，完成。");
    }

    /**
     * 下雨警報
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void rainAlarm() {
        if (!CRON_EXECUTE)
            return;
        String loactionName = "臺北市";
        WeatherResultPO po = openWeatherService.getWeatherInfo(loactionName, WeatherElementEnum.POP.getElement());
        for (WeatherElementPO weatherElementPO : po.getRecords().getLocation().get(0).getWeatherElement()) {
            for (TimePO timePO : weatherElementPO.getTime()) {   //取得隔天早上06:00 ~ 18:00 的機率
                Integer unit = Integer.valueOf(timePO.getParameter().getParameterName());
                //降雨機率大於70% 則通知
                if (unit >= 70) {
                    String start = DateUtils.parseDateTime(timePO.getStartTime(), DateUtils.yyyyMMddHHmmssDash, DateUtils.yyyyMMddHHmmDash);
                    String end = (DateUtils.parseDateTime(timePO.getEndTime(), DateUtils.yyyyMMddHHmmssDash, DateUtils.yyyyMMddHHmmDash)).split(" ")[1];
                    lineNotifySender.send(lineConfig.getLineNotifyKeyOwn(), start + " - " + end + "，降雨機率為: {unit}%，出門請帶傘。".replace("{unit}", unit.toString()));
                }
            }
        }
    }

    @Scheduled(cron = "0 0 15 * * ?")
    public void stockPriceCrawler() {
        if (!CRON_EXECUTE)
            return;
        twseApiService.initPriceMap();
        log.info("[schedule] 爬取最新股價，完成。");
    }

    //    @Scheduled(cron = "0 0 5 * * ?")
    public void javdbCrawler() {
        if (!CRON_EXECUTE)
            return;
        List<String> list = Arrays.asList(JavdbCrawlerService.TYPE_DAILY, JavdbCrawlerService.TYPE_WEEKLY, JavdbCrawlerService.TYPE_MONTHLY);
        List<CompletableFuture<Void>> futureList = new ArrayList<>(list.size());
        for (String type : list) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                javdbCrawlerService.crawlerRankings(type);
            }, crawlerExecutor).exceptionally(e -> {
                        log.error("爬取javdb，失敗! error msg:{}", e.getMessage());
                        return null;
                    }
            );
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        log.info("[schedule] 爬取javdb，完成。");
    }

    /**
     * 周一 至 周五 早上09:00 至 12:00，每25分鐘呼叫一次
     */
    @Scheduled(cron = "0 */20 9,10,11,12 ? * MON,TUE,WED,THU,FRI *")
    public void orderfoodHeartbeat() {
        if (!CRON_EXECUTE)
            return;
        orderfoodApiService.preventDormancy();
        log.info("[schedule] orderfoodHeartbeat，完成。");
    }
}
