package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.util.RandomUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Command({"吃什麼", "吃啥"})
public class EatWhatHandler implements CommandHandler {

    @Override
    public Message execute(String parameters) {
        List<String> listFood = Arrays.asList("日式料理", "拉麵", "牛排", "爭鮮", "火鍋",
                "7-11", "全家" , "麥當勞", "肯德基", "胖老爹", "披薩");
        String food = (String) RandomUtils.randomElement(listFood);
        return new TextMessage(food);
    }
}
