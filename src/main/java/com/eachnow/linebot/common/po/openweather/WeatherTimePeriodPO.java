package com.eachnow.linebot.common.po.openweather;

import com.eachnow.linebot.common.constant.TimePeriodEnum;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WeatherTimePeriodPO {
    private TimePO wx;
    private TimePO pop;
    private TimePO minT;
    private TimePO maxT;
    private TimePO ci;
    private boolean isRainy;            //該時段是否有下雨
    private String startTime;
    private String endTime;

}
