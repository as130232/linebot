package com.eachnow.linebot.common.util;

import java.sql.Timestamp;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.MinguoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    //    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC-4");    //美東
    public static final ZoneId CST_ZONE_ID = ZoneId.of("Asia/Taipei");  //台北
    public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMDash = DateTimeFormatter.ofPattern("yyyy-MM").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddDash = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmDash = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmssDash = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddSlash = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter yyyyMMddHHmmSlash = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(CST_ZONE_ID);
    public static final DateTimeFormatter hhmmss = DateTimeFormatter.ofPattern("HHmmss");
    public static final DateTimeFormatter hhmmssSemicolon = DateTimeFormatter.ofPattern("HH:mm:ss");
    //民國年
    public static final Chronology chronoByMinguo = MinguoChronology.INSTANCE;
    public static final DateTimeFormatter minguo = new DateTimeFormatterBuilder().parseLenient()
            .appendPattern("yyy年MM月dd日").toFormatter().withChronology(chronoByMinguo)
            .withDecimalStyle(DecimalStyle.of(Locale.getDefault()));

    public static final String START_OF_DAY = LocalTime.MIN.format(hhmmssSemicolon);
    public static final String END_OF_DAY = LocalTime.MAX.format(hhmmssSemicolon);

    public static String format(Timestamp timestamp, DateTimeFormatter formatter) {
        return formatter.format(timestamp.toLocalDateTime());
    }

    public static ZonedDateTime parseDate(String date, DateTimeFormatter formatter) {
        LocalDate parse = LocalDate.parse(date, formatter);
        return parse.atStartOfDay(CST_ZONE_ID);
    }

    public static String parseDate(String date, DateTimeFormatter from, DateTimeFormatter to) {
        LocalDate localDate = LocalDate.parse(date, from);
        return localDate.format(to);
    }

    /**
     * "2025-03-31 12:00:00" > "2025-03-31"
     */
    public static String parseDate(String time) {
        ZonedDateTime zonetime = ZonedDateTime.parse(time, DateUtils.yyyyMMddHHmmssDash);
        return zonetime.format(DateUtils.yyyyMMddDash);
    }

    public static String parseDateTime(String time) {
        ZonedDateTime zonetime = ZonedDateTime.parse(time, DateUtils.yyyyMMddHHmmssDash);
        return zonetime.format(DateUtils.yyyyMMddHHmmSlash);
    }

    public static ZonedDateTime parseDateTime(String dateTime, DateTimeFormatter from) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, from);
        return localDateTime.atZone(CST_ZONE_ID);
    }

    public static String parseDateTime(String dateTime, DateTimeFormatter from, DateTimeFormatter to) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, from);
        return localDateTime.atZone(CST_ZONE_ID).format(to);
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

    public static LocalDate parseByMinguo(String date) {
        ChronoLocalDate d1 = chronoByMinguo.date(minguo.parse(date));
        return LocalDate.from(d1);
    }

    /**
     * 取得台北當天日期
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


    /**
     * 取得台北當月
     *
     * @return yyyy-MM
     */
    public static String getCurrentMonth() {
        return ZonedDateTime.now(CST_ZONE_ID).format(yyyyMMDash);
    }

    /**
     * 取得台北上個月
     *
     * @return yyyy-MM
     */
    public static String getLastMonth() {
        return ZonedDateTime.now(CST_ZONE_ID).minusMonths(1).format(yyyyMMDash);
    }

    public static Timestamp getCurrentTime() {
        return new Timestamp((getCurrentEpochMilli()));
    }

    public static ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.now(CST_ZONE_ID);
    }

    /**
     * 判斷是否為今天
     */
    public static boolean isToday(long matchTimestamp) {
        long nowTimestamp = DateUtils.getCurrentEpochMilli();
        long diff = matchTimestamp - nowTimestamp;
        long dayTimestamp = TimeUnit.DAYS.toMillis(1L);
        return diff <= dayTimestamp;
    }

}
