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
    ;

    private String value;
    private String name;

    //爬取資源類型
    public static final Integer TYPE_ARTICLE = 1;
    public static final Integer TYPE_PICTURE = 2;
    public static final Integer TYPE_MAIN = 3;

    PttEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getUrl(PttEnum pttEnum) {
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

}
