package com.eachnow.linebot.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParamterUtils {
    //串接參數的字串
    public static String CONTACT = " ";
    public static String parseCommand(String text) {
        return text.split(CONTACT)[0];
    }

    public static List<String> parse(String text) {
        String[] paramArr = text.split(CONTACT);
        return new ArrayList<>(Arrays.asList(paramArr));
    }

    /**
     * 根據空格切參數，取得第一個參數(除command外)
     *
     * @param text
     */
    public static String getIndexOneParameter(String text) {
        List<String> list = parse(text);
        if (list.size() > 1)
            return list.get(1); //index:0 為指令
        return null;
    }

    /**
     * 根據空格切參數，取得所有參數(除command外)
     *
     * @param text
     */
    public static List<String> listParameter(String text) {
        List<String> list = parse(text);
        if (list.size() > 1) {
            list.remove(0); //移除掉command
            return list;
        }
        return new ArrayList<>();
    }
}
