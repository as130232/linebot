package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
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
    public Message execute(CommandPO commandPO) {
        List<String> listFood = Arrays.asList("日式料理", "燒烤", "壽喜燒", "拉麵", "居酒屋", "牛排", "餐酒館", "快炒店",
                "火鍋", "披薩", "牛肉麵", "早午餐", "中式料理", "港式料理", "韓式料理", "越式料理", "素食", "夜市",
                "7-11", "全家", "麥當勞", "肯德基", "胖老爹", "摩斯漢堡",
                "爭鮮", "海力士", "豚醬拉麵",
                "加分鍋物", "石二鍋", "京賀家壽喜燒", "四海遊龍",
                "茗香園", "段純真", "阿達師牛肉麵", "貳樓", "晴美美", "麥味登", "QBurger");
        String food = (String) RandomUtils.randomElement(listFood);
        return new TextMessage(food);
    }
}
