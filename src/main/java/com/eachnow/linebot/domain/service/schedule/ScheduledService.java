package com.eachnow.linebot.domain.service.schedule;

import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.openweather.TimePO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.FlexMessageUtils;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.FemasService;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.crawler.JavdbCrawlerService;
import com.eachnow.linebot.domain.service.gateway.OrderfoodApiService;
import com.eachnow.linebot.domain.service.gateway.TwseApiService;
import com.eachnow.linebot.domain.service.gateway.WeatherApiService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.line.MessageSender;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

;


/**
 * 排程服務
 */
@Slf4j
@Component
public class ScheduledService {
    @Value("${cron.flag:false}")
    private boolean CRON_EXECUTE;
    private final ThreadPoolExecutor crawlerExecutor;
    private final LineConfig lineConfig;
    private final LineNotifySender lineNotifySender;
    private final BeautyCrawlerService beautyCrawlerService;
    private final WeatherApiService weatherApiService;
    private final TwseApiService twseApiService;
    private final JavdbCrawlerService javdbCrawlerService;
    private final OrderfoodApiService orderfoodApiService;
    private final FemasService femasService;
    private final MessageSender messageSender;

    @Autowired
    public ScheduledService(@Qualifier("ptt-crawler-executor") ThreadPoolExecutor crawlerExecutor,
                            LineConfig lineConfig,
                            LineNotifySender lineNotifySender,
                            BeautyCrawlerService beautyCrawlerService,
                            WeatherApiService weatherApiService,
                            TwseApiService twseApiService,
                            JavdbCrawlerService javdbCrawlerService,
                            OrderfoodApiService orderfoodApiService,
                            FemasService femasService, MessageSender messageSender) {
        this.crawlerExecutor = crawlerExecutor;
        this.lineConfig = lineConfig;
        this.lineNotifySender = lineNotifySender;
        this.beautyCrawlerService = beautyCrawlerService;
        this.weatherApiService = weatherApiService;
        this.twseApiService = twseApiService;
        this.javdbCrawlerService = javdbCrawlerService;
        this.orderfoodApiService = orderfoodApiService;
        this.femasService = femasService;
        this.messageSender = messageSender;
    }

    public void switchCron(boolean isOpen) {
        CRON_EXECUTE = isOpen;
    }

    @Scheduled(cron = "0 0 8,14,20 * * ?")
    public void beautyCrawler() {
        if (!CRON_EXECUTE)
            return;
        beautyCrawlerService.crawler(10);
        log.info("[schedule] 爬取表特版，完成。");
    }

    /**
     * 下雨警報 v1 純文字版本，較費流量次數
     * 暫時不用
     */
//    @Scheduled(cron = "0 0 23 * * ?")
    public void rainAlarmForText() {
        if (!CRON_EXECUTE)
            return;
        String locationName = "臺北市";
        WeatherResultPO po = weatherApiService.getWeatherInfo(locationName, WeatherElementEnum.POP.getElement());
        for (WeatherElementPO weatherElementPO : po.getRecords().getLocation().get(0).getWeatherElement()) {
            for (TimePO timePO : weatherElementPO.getTime()) {   //取得隔天早上06:00 ~ 18:00 的機率
                int unit = Integer.parseInt(timePO.getParameter().getParameterName());
                //降雨機率大於70% 則通知
                if (unit >= 70) {
                    String start = DateUtils.parseDateTime(timePO.getStartTime(), DateUtils.yyyyMMddHHmmssDash, DateUtils.yyyyMMddHHmmSlash);
                    String end = (DateUtils.parseDateTime(timePO.getEndTime(), DateUtils.yyyyMMddHHmmssDash, DateUtils.yyyyMMddHHmmSlash)).split(" ")[1];
                    String message = start + " - " + end + "，降雨機率為: {unit}%，出門請帶傘。".replace("{unit}", Integer.toString(unit));
//                    lineNotifySender.sendToCharles(message);
                    messageSender.pushTextToCharles(message);
                }
            }
        }
    }

    /**
     * 下雨警報 v2 天氣圖卡，較省流量次數
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void rainAlarm() {
        if (!CRON_EXECUTE)
            return;
        String locationName = "臺北市";
        WeatherResultPO po = weatherApiService.getWeatherInfo(locationName, null);
        //因為line有限制主動發送訊息次數，現階段有下雨才發送，沒下雨就不送
        boolean isRain = weatherApiService.isRain(po);
        log.info("ready to rainAlarm, isRain:{}", isRain);
        if (!isRain) {
            return;
        }
        Message message = FlexMessageUtils.getWeatherCard(po);
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        messageSender.push(lineConfig.getLineUserIdOwn(), messages);
    }

    @Scheduled(cron = "0 0 15 * * ?")
    public void stockPriceCrawler() {
        if (!CRON_EXECUTE)
            return;
        twseApiService.initPriceMap();
        log.info("[schedule] 爬取最新股價，完成。");
    }

    /**
     * 爬取javDB
     */
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
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        log.info("[schedule] 爬取javdb，完成。");
    }

    /**
     * 上班未打卡提醒
     * 周一 至 周五 早上 09:50 檢查是否有打卡記錄
     */
//    @Scheduled(cron = "0 50 9 * * MON-FRI")
    public void remindPunchIn() {
        if (!CRON_EXECUTE)
            return;
        femasService.remindPunchIn();
    }

    /**
     * 設置下班提醒
     * 周一 至 周五 早上10:00 至 13:00，每30分鐘呼叫一次
     */
    @Scheduled(cron = "0 */30 10-13 ? * MON-FRI")
    public void remindPunchOut() {
        if (!CRON_EXECUTE)
            return;
        femasService.remindPunchOut();
    }

    /**
     * 每月一號早上十點，檢查上個月是否有遲到，需提醒送忘刷單或請假
     */
    @Scheduled(cron = "0 0 10 1 * ?")
    public void checkWorkLateLastMonth() {
        if (!CRON_EXECUTE)
            return;
        femasService.checkWorkLateLastMonth();
    }
}
