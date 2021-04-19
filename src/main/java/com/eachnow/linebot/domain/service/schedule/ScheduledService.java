package com.eachnow.linebot.domain.service.schedule;

import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.openweather.TimePO;
import com.eachnow.linebot.common.po.openweather.WeatherElementPO;
import com.eachnow.linebot.common.po.openweather.WeatherResultPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.config.LineConfig;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.gateway.OpenWeatherService;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
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
    private LineNotifySender lineNotifySender;
    private BeautyCrawlerService beautyCrawlerService;
    private OpenWeatherService openWeatherService;
    private LineConfig lineConfig;

    @Autowired
    public ScheduledService(LineNotifySender lineNotifySender,
                            BeautyCrawlerService beautyCrawlerService,
                            OpenWeatherService openWeatherService,
                            LineConfig lineConfig) {
        this.lineNotifySender = lineNotifySender;
        this.beautyCrawlerService = beautyCrawlerService;
        this.openWeatherService = openWeatherService;
        this.lineConfig = lineConfig;
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
    @Scheduled(cron = "0 0 8,23 * * ?")
    public void rainAlarm() {
        String loactionName = "臺北市";
        WeatherResultPO po = openWeatherService.getWeatherInfo(loactionName, WeatherElementEnum.POP.getElement());
        ZonedDateTime zonedDateTime = DateUtils.getCurrentDateTime();
        Integer currentDay = zonedDateTime.getDayOfMonth();
        Integer currentHour = zonedDateTime.getHour();
        for (WeatherElementPO weatherElementPO : po.getRecords().getLocation().get(0).getWeatherElement()) {
            for (TimePO timePO : weatherElementPO.getTime()) {   //取得隔天早上06:00 ~ 18:00 的機率
                ZonedDateTime startTime = DateUtils.parseDate(timePO.getStartTime(), DateUtils.yyyyMMddHHmmssDash);
                if (currentHour == 23 && (currentDay + 1) == startTime.getDayOfMonth()
                        || currentHour == 8 && currentDay == startTime.getDayOfMonth()) {
                    if (timePO.getStartTime().contains("06:00:00") && timePO.getEndTime().contains("18:00:00")
                            //降雨機率大於60% 則通知
                            && Integer.valueOf(timePO.getParameter().getParameterUnit()) >= 60) {
                        lineNotifySender.send(lineConfig.getLineNotifyKeyOwn(), "降雨機率為: {unit}%，請記得帶傘。".replace("{unit}", unit.toString()));
                        break;
                    }
                }
            }
        }
    }

}
