package com.eachnow.linebot.common.util;

public class CommandUtils {

    public static String parseCommand(String text){
        return text.split(" ")[0];
    }
}
