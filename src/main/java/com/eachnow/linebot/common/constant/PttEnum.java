package com.eachnow.linebot.common.constant;

import lombok.Getter;

@Getter
public enum PttEnum {

    MAIN("main", "熱門"),
    GOSSIPING("gossiping", "八卦"),
    BEAUTY("beauty", "表特"),
    JAPANAVGIRLS("japanavgirls", "女優"),
    SEX("sex", "西斯"),
    MOVIE("movie", "電影"),
    STOCK("stock", "股票"),
    TECH_JOB("tech_job", "科技"),
    SOFT_JOB("soft_job", "軟體"),
    PC_SHOPPING("PC_Shopping", "電蝦"),
    E_SHOPPING("e-shopping", "購物"),
    HATE_POLITICS("HatePolitics", "政黑"),
    WOMEN_TALK("WomenTalk", "女人"),
    JOKE("Joke", "就可"),
    STUPID("Stupid", "笨版"),
    CPLIFE("CPLife", "省錢"),
    CAR("Car", "汽車"),
    BOY_GIRL("Boy-Girl", "男女"),
    NBA("NBA", "NBA"),

    ;

    private final String value;
    private final String name;

    //爬取資源類型
    public static final Integer TYPE_ARTICLE = 1;
    public static final Integer TYPE_PICTURE = 2;
    public static final Integer TYPE_MAIN = 3;

    PttEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getUrl(String board) {
        String dispUrl = "https://disp.cc/b/%s";
        return String.format(dispUrl, board);
    }

    public static String getUrlByBbs(PttEnum pttEnum) {
        String bbsUrl = "https://www.ptt.cc/bbs/%s/index.html";
        return String.format(bbsUrl, pttEnum.getValue());
    }

    public static String getUrlByDisp(PttEnum pttEnum) {
        String dispUrl = "https://disp.cc/b/%s";
        return String.format(dispUrl, pttEnum.getValue());
    }

    public static PttEnum getPttEnum(String name) {
        for (PttEnum pttEnum : PttEnum.values()) {
            if (pttEnum.getName().equals(name)) {
                return pttEnum;
            }
        }
        return PttEnum.GOSSIPING;
    }

    public static PttEnum getPttEnumByValue(String value) {
        for (PttEnum pttEnum : PttEnum.values()) {
            if (pttEnum.getValue().equalsIgnoreCase(value)) {
                return pttEnum;
            }
        }
        return PttEnum.GOSSIPING;
    }
}
