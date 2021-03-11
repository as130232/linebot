package com.eachnow.linebot.common.util;

import java.util.Arrays;
import java.util.List;

public class ParamterUtils {

    public static String parseCommand(String text){
        return text.split(" ")[0];
    }

    public static List<String> parse(String text) {
        String[] paramArr = text.split(" ");
        return Arrays.asList(paramArr);
    }

    public static String getParameter(String text) {
        List<String> list = parse(text);
        if (list.size() > 1)
            return list.get(1); //index:0 為指令
        return null;
    }
}
