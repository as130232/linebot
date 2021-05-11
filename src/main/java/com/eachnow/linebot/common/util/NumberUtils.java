package com.eachnow.linebot.common.util;

import java.math.BigDecimal;
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

    public static void main(String[] args) {
//        BigDecimal openPrice = new BigDecimal("415.50");
//        BigDecimal closePrice = new BigDecimal("393.00");
//        BigDecimal result = ((closePrice.subtract(openPrice)).divide(openPrice, 5, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100l))).setScale(2, BigDecimal.ROUND_HALF_UP);
//        float a = result.floatValue();
//        System.out.println("a:" + a);


        BigDecimal highestPrice = new BigDecimal("597");
        BigDecimal lowestPrice = new BigDecimal("588");
        BigDecimal closePrice = new BigDecimal("599");
        BigDecimal result2 = ((highestPrice.subtract(lowestPrice)).divide(closePrice, 5, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100l))).setScale(2, BigDecimal.ROUND_HALF_UP);
        float b = result2.floatValue();
        System.out.println("b:" + b);
    }
}
