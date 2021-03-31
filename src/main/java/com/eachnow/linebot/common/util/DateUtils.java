package com.eachnow.linebot.common.util;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    //    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC-4");    //美東
    public static final ZoneId CST_ZONE_ID = ZoneId.of("Asia/Taipei");  //台北
    public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddDash = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmDash = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmssDash = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter hhmmss = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final String START_OF_DAY = LocalTime.MIN.format(hhmmss);
    public static final String END_OF_DAY = LocalTime.MAX.format(hhmmss);

    public static String format(Timestamp timestamp, DateTimeFormatter formatter) {
        return formatter.format(timestamp.toLocalDateTime());
    }

    public static ZonedDateTime parseDate(String date, DateTimeFormatter formatter) {
        LocalDate parse = LocalDate.parse(date, formatter);
        return parse.atStartOfDay(CST_ZONE_ID);
    }

    public static long parseDateTimeToMilli(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, yyyyMMddHHmmssDash);
        return localDateTime.atZone(CST_ZONE_ID).toInstant().toEpochMilli();
    }

    /**
     * 根據formatter將日期字串轉成 yyyy-MM-dd 00:00:00
     */
    public static long parseToStartOfDayMilli(String date, DateTimeFormatter formatter) {
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.atTime(LocalTime.MIN).atZone(CST_ZONE_ID).toInstant().toEpochMilli();
    }

    /**
     * 根據formatter將日期字串轉成 yyyy-MM-dd 23:59:59
     */
    public static long parseToEndOfDayMilli(String date, DateTimeFormatter formatter) {
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.atTime(LocalTime.MAX).atZone(CST_ZONE_ID).toInstant().toEpochMilli();
    }

    /**
     * 取得台北現在日期格式
     *
     * @return yyyy-MM-dd
     */
    public static String getCurrentDate() {
        return ZonedDateTime.now(CST_ZONE_ID).format(yyyyMMddDash);
    }

    public static String getCurrentDate(DateTimeFormatter formatter) {
        return ZonedDateTime.now(CST_ZONE_ID).format(formatter);
    }

    /**
     * 取得台北現在時間
     *
     * @return epochMilli
     */
    public static long getCurrentEpochMilli() {
        return ZonedDateTime.now(CST_ZONE_ID).toInstant().toEpochMilli();
    }


}
