package com.eachnow.linebot.common.util;

import com.eachnow.linebot.common.constant.TimePeriodEnum;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.MessagePO;
import com.eachnow.linebot.common.po.PushMessagePO;
import com.eachnow.linebot.common.po.openweather.*;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.JsonUtils;
import com.eachnow.linebot.config.LineConfig;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Filler;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class FlexMessageUtils {

    /**
     * 取得天氣卡
     */
    public static Message getWeatherCard(WeatherResultPO weatherResultPO) {
        LocationPO locationPO = weatherResultPO.getRecords().getLocation().get(0);
        String date = DateUtils.parseDate(locationPO.getWeatherElement().get(0).getTime().get(0).getStartTime());
        Map<String, WeatherTimePeriodPO> weatherTimePeriodMap = new HashMap<>();
        //將同一時段歸類在一起
        for (WeatherElementPO weatherElementPO : locationPO.getWeatherElement()) {
            for (TimePO timePO : weatherElementPO.getTime()) {
                TimePeriodEnum startPeriodEnum = TimePeriodEnum.getTimePeriod(timePO.getStartTime());
                assert startPeriodEnum != null;
                String key = timePO.getStartTime();
                WeatherTimePeriodPO weatherTimePeriodPO = weatherTimePeriodMap.get(key);
                if (weatherTimePeriodPO == null) {
                    weatherTimePeriodPO = WeatherTimePeriodPO.builder().isRainy(false).startTime(timePO.getStartTime()).endTime(timePO.getEndTime()).build();
                }

                if (WeatherElementEnum.WX.getElement().equals(weatherElementPO.getElementName())) {
                    weatherTimePeriodPO.setWx(timePO);
                } else if (WeatherElementEnum.MAX_T.getElement().equals(weatherElementPO.getElementName())) {
                    weatherTimePeriodPO.setMaxT(timePO);
                } else if (WeatherElementEnum.MIN_T.getElement().equals(weatherElementPO.getElementName())) {
                    weatherTimePeriodPO.setMinT(timePO);
                } else if (WeatherElementEnum.CI.getElement().equals(weatherElementPO.getElementName())) {
                    weatherTimePeriodPO.setCi(timePO);
                } else if (WeatherElementEnum.POP.getElement().equals(weatherElementPO.getElementName())) {
                    weatherTimePeriodPO.setPop(timePO);
                    int unit = Integer.parseInt(timePO.getParameter().getParameterName());
                    //降雨機率大於70% 則標註下雨
                    if (unit >= 70) {
                        weatherTimePeriodPO.setRainy(true);
                    }
                }
                weatherTimePeriodMap.put(key, weatherTimePeriodPO);
            }
        }
        String weatherIcon = "☀️";
        //取得明天白天(period:早上/中午)後的天氣狀況
        if (weatherTimePeriodMap.get(TimePeriodEnum.MORNING.getName()) != null) {
            WeatherTimePeriodPO weatherTimePeriodPO = weatherTimePeriodMap.get(TimePeriodEnum.MORNING.getName());
            int unit = Integer.parseInt(weatherTimePeriodPO.getPop().getParameter().getParameterName());
            weatherIcon = WeatherElementEnum.getRainIcon(unit);
        } else if (weatherTimePeriodMap.get(TimePeriodEnum.NOON.getName()) != null) {
            WeatherTimePeriodPO weatherTimePeriodPO = weatherTimePeriodMap.get(TimePeriodEnum.NOON.getName());
            int unit = Integer.parseInt(weatherTimePeriodPO.getPop().getParameter().getParameterName());
            weatherIcon = WeatherElementEnum.getRainIcon(unit);
        }

        List<FlexComponent> bodyContents = new ArrayList<>(3);
        Box dateAndLocationBox = Box.builder().layout(FlexLayout.HORIZONTAL).contents(Arrays.asList(
                //日期
                Text.builder().text(date).size(FlexFontSize.SM).flex(1).style(Text.TextStyle.ITALIC).gravity(FlexGravity.CENTER).build(),
                //地點
                Text.builder().text(locationPO.getLocationName()).size(FlexFontSize.Md).flex(1).align(FlexAlign.CENTER).style(Text.TextStyle.ITALIC).build(),
                //天氣圖
                Text.builder().text(weatherIcon).size(FlexFontSize.Md).flex(1).align(FlexAlign.CENTER).build()
        )).build();
        bodyContents.add(dateAndLocationBox);
        //根據時間排序，時間近的在前面
        List<WeatherTimePeriodPO> weatherTimePeriodList = new ArrayList<>(weatherTimePeriodMap.values());
        weatherTimePeriodList.sort(Comparator.comparing(weatherTimePeriodPO ->
                DateUtils.parseDateTimeToMilli(weatherTimePeriodPO.getStartTime())
        ));
        boolean rainAlert = false;          // 是否需要顯示下雨天提醒帶傘
        int index = 1;
        for (WeatherTimePeriodPO po : weatherTimePeriodList) {
            String startTime = po.getStartTime();
            WeatherTimePeriodPO weatherTimePeriodPO = weatherTimePeriodMap.get(startTime);
            TimePeriodEnum startPeriodEnum = TimePeriodEnum.getTimePeriod(startTime);
            assert startPeriodEnum != null;
            String key = startPeriodEnum.getName();
            boolean isActivityPeriod = false;   // 是否為活動時段
            if (TimePeriodEnum.MORNING.getName().equals(key) || TimePeriodEnum.NOON.getName().equals(key)) {
                isActivityPeriod = true;
                if (weatherTimePeriodPO.isRainy()) {
                    rainAlert = true;
                }
            }
            List<FlexComponent> timePeriodContents = new ArrayList<>(4);

            //1. 時間段
            //時間段字串
            timePeriodContents.add(Text.builder().text(startPeriodEnum.getPeriod()).size(FlexFontSize.SM).gravity(FlexGravity.CENTER).build());
            //圓圈icon
            //初始是紅色，其餘藍色
            String circleIconColor = index == 1 ? "#EF454D" : "#6486E3";
            Box circleIconBox = Box.builder().layout(FlexLayout.VERTICAL).contents(Arrays.asList(
                    Filler.builder().build(),
                    Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(Filler.builder().build())).cornerRadius("30px").height("12px").width("12px").borderColor(circleIconColor).borderWidth("2px").build(),
                    Filler.builder().build()
            )).flex(0).build();
            timePeriodContents.add(circleIconBox);
            //時間段中文字串
            boolean isToday = DateUtils.isToday(DateUtils.parseDateTimeToMilli(startTime));
            String periodName = (isToday ? "" : "翌日 ") + startPeriodEnum.getName();
            timePeriodContents.add(Text.builder().text(periodName).size(FlexFontSize.SM).gravity(FlexGravity.CENTER).flex(5).weight(Text.TextWeight.BOLD).build());
            Box timePeriodBox = Box.builder().layout(FlexLayout.HORIZONTAL).contents(timePeriodContents).spacing(FlexMarginSize.LG).paddingAll(FlexPaddingSize.XS).build();
            bodyContents.add(timePeriodBox);

            //2. 天氣資訊
            List<FlexComponent> weatherInfoContent = new ArrayList<>(3);

            //空白區域(可加入icon，標示時段為白天時段或是否下雨)
            Box blankBox = Box.builder().layout(FlexLayout.BASELINE).flex(1).contents(Collections.singletonList(Filler.builder().build())).build();
            weatherInfoContent.add(blankBox);
            //直線
            List<FlexComponent> straightLineContent = Collections.singletonList(
                    Box.builder().layout(FlexLayout.HORIZONTAL).flex(1).contents(Arrays.asList(
                            Filler.builder().build(),
                            Box.builder().layout(FlexLayout.VERTICAL).width("2px").backgroundColor("#B7B7B7").contents(Collections.singletonList(Filler.builder().build())).build(),
                            Filler.builder().build())
                    ).build());
            Box straightLineBox = Box.builder().layout(FlexLayout.VERTICAL).flex(1).width("12px").contents(straightLineContent).build();
            weatherInfoContent.add(straightLineBox);
            //天氣資訊
            String fontColor = isActivityPeriod ? "#000000" : "#8c8c8c"; //若是白天，則黑字反之灰字較不顯眼
            String pop = "降雨：" + weatherTimePeriodPO.getPop().getParameter().getParameterName() + "%";
            String minToMaxTemperature = "溫度：{min}℃ - {max}℃".replace("{min}", weatherTimePeriodPO.getMinT().getParameter().getParameterName()).replace("{max}", weatherTimePeriodPO.getMaxT().getParameter().getParameterName());
            String wxAndCi = weatherTimePeriodPO.getWx().getParameter().getParameterName() + ", " + weatherTimePeriodPO.getCi().getParameter().getParameterName();
            Box weatherInfoBox = Box.builder().layout(FlexLayout.VERTICAL).flex(5).contents(Arrays.asList(
                    Text.builder().text(pop).flex(4).size(FlexFontSize.XS).color(fontColor).gravity(FlexGravity.CENTER).build(),
                    Text.builder().text(minToMaxTemperature).flex(4).size(FlexFontSize.XS).color(fontColor).gravity(FlexGravity.CENTER).build(),
                    Text.builder().text(wxAndCi).flex(4).size(FlexFontSize.XS).color(fontColor).gravity(FlexGravity.CENTER).build()
            )).build();
            weatherInfoContent.add(weatherInfoBox);

            Box allWeatherInfoBox = Box.builder().layout(FlexLayout.HORIZONTAL).contents(weatherInfoContent).spacing(FlexMarginSize.LG).height("64px").paddingAll(FlexPaddingSize.XS).build();
            bodyContents.add(allWeatherInfoBox);
            index = index + 1;
        }
        //最後結束需要在添加一筆結束時間段
        List<FlexComponent> endTimePeriodContents = new ArrayList<>(4);
        WeatherTimePeriodPO lastWeatherTimePeriod = weatherTimePeriodList.get(weatherTimePeriodList.size() - 1);
        //3. 結尾時間段
        TimePeriodEnum endPeriodEnum = TimePeriodEnum.getTimePeriod(lastWeatherTimePeriod.getEndTime());
        assert endPeriodEnum != null;
        //時間段字串
        endTimePeriodContents.add(Text.builder().text(endPeriodEnum.getPeriod()).size(FlexFontSize.SM).gravity(FlexGravity.CENTER).build());
        //圓圈icon
        String circleIconColor = "#EF454D";
        Box circleIconBox = Box.builder().layout(FlexLayout.VERTICAL).contents(Arrays.asList(
                Filler.builder().build(),
                Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(Filler.builder().build())).cornerRadius("30px").height("12px").width("12px").borderColor(circleIconColor).borderWidth("2px").build(),
                Filler.builder().build()
        )).flex(0).build();
        endTimePeriodContents.add(circleIconBox);
        //時間段中文字串
        boolean isToday = DateUtils.isToday(DateUtils.parseDateTimeToMilli(lastWeatherTimePeriod.getEndTime()));
        String periodName = isToday ? "" : "翌日 " + endPeriodEnum.getName();
        endTimePeriodContents.add(Text.builder().text(periodName).size(FlexFontSize.SM).gravity(FlexGravity.CENTER).flex(5).weight(Text.TextWeight.BOLD).build());
        Box timePeriodBox = Box.builder().layout(FlexLayout.HORIZONTAL).contents(endTimePeriodContents).spacing(FlexMarginSize.LG).paddingAll(FlexPaddingSize.XS).build();
        bodyContents.add(timePeriodBox);


        Box footer = null;
        if (rainAlert) {
            footer = Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(
                    Text.builder().text("☔️ 明日白天下雨，出門請帶傘 ☔️").color("#ffffff").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).build()
            )).spacing(FlexMarginSize.MD).paddingAll(FlexPaddingSize.MD).backgroundColor("#e82665").build();
        } else {
            //因為line有限制主動發送訊息次數，現階段有下雨才發送，沒下雨就不送
            return null;
        }
        //標頭
        List<FlexComponent> headerContents = Collections.singletonList(Text.builder().text("天氣卡").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#47bdd9").build();
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.LG).build();

        FlexContainer contents = Bubble.builder().size(Bubble.BubbleSize.MEGA).header(header).hero(null).body(body).footer(footer).build();
        return FlexMessage.builder().altText("天氣卡").contents(contents).build();
    }
}
