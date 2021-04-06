package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.RandomUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Command({"吃什麼", "吃啥"})
public class EatWhatHandler implements CommandHandler {

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> listFood = Arrays.asList("日式料理", "燒烤", "壽喜燒", "拉麵", "居酒屋", "牛排", "餐酒館", "快炒店",
                "火鍋", "披薩", "牛肉麵", "早午餐", "中式料理", "港式料理", "韓式料理", "越式料理", "素食", "夜市",
                "7-11", "全家", "麥當勞", "肯德基", "胖老爹", "摩斯漢堡",
                "爭鮮", "海力士", "豚醬拉麵",
                "加分鍋物", "石二鍋", "京賀家壽喜燒", "四海遊龍", "老秈味石磨腸粉",
                "茗香園", "段純真", "阿達師牛肉麵", "貳樓", "晴美美", "麥味登", "QBurger");
        Map<String, List<String>> result = new HashMap<>();
        result.put("中式料理", Arrays.asList(""));
        result.put("港式料理", Arrays.asList("茗香園"));
        result.put("韓式料理", Arrays.asList("石二鍋", "兩餐"));
        result.put("越式料理", Arrays.asList("初越越南河粉"));
        result.put("西洋料理", Arrays.asList("貳樓餐廳"));
        result.put("日式料理", Arrays.asList("爭鮮", "海力士", "豚醬拉麵", "京賀家壽喜燒"));
        result.put("早午餐", Arrays.asList("麥味登", "QBurger"));
        result.put("火鍋", Arrays.asList("加分鍋物"));
        result.put("燒烤", Arrays.asList(""));
        result.put("牛肉麵", Arrays.asList("阿達師牛肉麵", "段純真"));
        result.put("素食", Arrays.asList(""));
        result.put("速食", Arrays.asList("麥當勞", "肯德基", "胖老爹", "摩斯漢堡"));
        result.put("餐酒館", Arrays.asList(""));
        result.put("酒吧", Arrays.asList("噶瑪蘭威士忌酒吧"));
        String food = (String) RandomUtils.randomElement(listFood);
        return new TextMessage(food);
    }
}
