package com.eachnow.linebot.domain.service.schedule;

import com.eachnow.linebot.common.constant.LineNotifyConstant;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.openweather.TimePO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import com.eachnow.linebot.domain.service.line.LineNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

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
    private LineNotifyService lineNotifyService;
    private BeautyCrawlerService beautyCrawlerService;
    private OpenWeatherService openWeatherService;

    @Autowired
    public ScheduledService(LineNotifyService lineNotifyService,
                            BeautyCrawlerService beautyCrawlerService,
                            OpenWeatherService openWeatherService) {
        this.lineNotifyService = lineNotifyService;
        this.beautyCrawlerService = beautyCrawlerService;
        this.openWeatherService = openWeatherService;
    }

    public void switchCron(boolean isOpen) {
        CRON_EXECUTE = isOpen;
    }

    public boolean getCron() {
        return CRON_EXECUTE;
    }

    @Scheduled(cron = "${schedule.beauty.cron}")
    public void beautyCrawler() {
        if (!CRON_EXECUTE)
            return;
        log.info("[schedule]準備爬取表特版。time:{}", new Date());
        beautyCrawlerService.crawler(3);
        log.info("[schedule]爬取表特版，完成。time:{}", new Date());
    }

    /**
     * 下雨警報
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void rainAlarm() {
        String loactionName = "臺北市";
        WeatherResultPO po = openWeatherService.getWeatherInfo(loactionName, null);
        for (WeatherElementPO weatherElementPO : po.getRecords().getLocation().get(0).getWeatherElement()) {
            //降雨
            if (WeatherElementEnum.POP.getElement().equals(weatherElementPO.getElementName())) {
                TimePO timePO = weatherElementPO.getTime().get(1);  // 取得隔天早上06:00 ~ 18:00 的機率
                Integer unit = Integer.valueOf(timePO.getParameter().getParameterUnit());
                if (unit >= 70) {   //降雨機率大於70% 則通知
                    lineNotifyService.send(LineNotifyConstant.OWN, "明天降雨機率為: {unit}%，記得帶傘。".replace("{unit}", unit.toString()));
                }
                break;
            }
        }
    }

    /**
     * 自動記帳:iCloud 4/4 13:00 $90
     */

}
