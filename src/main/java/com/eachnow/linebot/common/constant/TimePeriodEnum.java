package com.eachnow.linebot.common.constant;

import com.eachnow.linebot.common.util.DateUtils;
import lombok.Getter;

@Getter
public enum TimePeriodEnum {
    EARLY_MORNING("凌晨", "00:00"),
    MORNING("早上", "06:00"),
    NOON("中午", "12:00"),
    EVENING("傍晚", "18:00"),
    NEXT_DAY("翌日", "00:00"),

    ;
    private String name;
    private String period;

    TimePeriodEnum(String name, String period) {
        this.name = name;
        this.period = period;
    }

    /**
     * 取得對應時間區段列舉
     *
     * @param startTime "2025-03-31 12:00:00"
     */
    public static TimePeriodEnum getTimePeriod(String startTime) {
        if (startTime.contains("00:00:00")) {
            //凌晨或是翌日時間段一樣，檢查是今日日期或明天
            if (DateUtils.isToday(DateUtils.parseDateTimeToMilli(startTime))) {
                return EARLY_MORNING;
            } else {
                return NEXT_DAY;
            }
        }
        for (TimePeriodEnum periodEnum : TimePeriodEnum.values()) {
            if (!periodEnum.equals(EARLY_MORNING) && !periodEnum.equals(NEXT_DAY) &&
                    startTime.contains(periodEnum.getPeriod())) {
                return periodEnum;
            }
        }
        return null;
    }
}
