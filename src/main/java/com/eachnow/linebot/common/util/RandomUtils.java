package com.eachnow.linebot.common.util;

import java.util.List;
import java.util.Random;

public class RandomUtils {
    public static Object randomElement(List<?> list){
        int item = new Random().nextInt(list.size());
        return list.get(item);
    }
}
