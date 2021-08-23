package com.eachnow.linebot.common.constant;

import lombok.Getter;

@Getter
public class PttConstant {
    //爬取資源類型
    public static final Integer TYPE_ARTICLE = 1;
    public static final Integer TYPE_PICTURE = 2;
    public static final Integer TYPE_MAIN = 3;
    //PTT網址
    public static final String GOSSIPING_URL = "https://www.ptt.cc/bbs/Gossiping/index.html";
    public static final String BEAUTY_URL = "https://www.ptt.cc/bbs/Beauty/index.html";
    public static final String AVGIRLS_URL = "https://www.ptt.cc/bbs/Japanavgirls/index.html";
    public static final String MOVIE_URL = "https://www.ptt.cc/bbs/movie/index.html";

    //PTT網址(DISP)
    public static final String MAIN_DISP_URL = "https://disp.cc/b/main";
    public static final String SEX_DISP_URL = "https://disp.cc/b/sex";

    public String getUrl(String classType) {
        String bbsUrl = "https://www.ptt.cc/bbs/%s/index.html";
        return String.format(bbsUrl, classType);
    }

    public String getUrlByDisp(String classType) {
        String dispUrl = "https://disp.cc/b/%s";
        return String.format(dispUrl, classType);
    }


}
