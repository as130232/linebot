package com.eachnow.linebot.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC-4");    //美東
    public static final ZoneId CST_ZONE_ID = ZoneId.of("Asia/Taipei");  //台北
    public static final DateTimeFormatter yyyyMMddDash = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(DEFAULT_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmDash = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmssDash = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(CST_ZONE_ID);


    public static ZonedDateTime parseDate(String date) {
        LocalDate parse = LocalDate.parse(date, yyyyMMddDash);
        return parse.atStartOfDay(DEFAULT_ZONE_ID);
    }

    public static long parseDateToMilli(String date) {
        return parseDate(date).toInstant().toEpochMilli();
    }

    /**
     * 取得台北現在日期格式
     *
     * @return yyyy-MM-dd
     */
    public static String getCurrentDate() {
        return ZonedDateTime.now(CST_ZONE_ID).format(yyyyMMddDash);
    }

    public static long getCurrentEpochMilli() {
        return ZonedDateTime.now(CST_ZONE_ID).toInstant().toEpochMilli();
    }


    public static long getTimestampByDate(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, yyyyMMddHHmmDash);
        return localDateTime.atZone(CST_ZONE_ID).toInstant().toEpochMilli();
    }
    public static long getTimestampByDatetime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, yyyyMMddHHmmssDash);
        return localDateTime.atZone(CST_ZONE_ID).toInstant().toEpochMilli();
    }

}
