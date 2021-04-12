package com.eachnow.linebot.common.util;

import java.util.regex.Pattern;

public class NumberUtils {
    /**
     * 判斷該字串是否為整數或浮點數
     *
     * @param input 字串
     * @return 是返回true 否則返回false
     */
    public static boolean isNumber(String input) {
        if (input == null || "".equals(input)) {
            return false;
        }
        return Pattern.matches("[0-9]*(\\.?)[0-9]*", input);
    }
}
