package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.util.RandomUtils;
import com.eachnow.linebot.domain.service.crawler.BeautyCrawlerService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Command({"hi", "hello", "你好", "哈囉"})
public class HelloHandler implements CommandHandler {

    @Override
    public Message execute(String parameters) {
        List<String> list = Arrays.asList("安安", "你好呀魯蛇～", "你是天線寶寶嗎？太陽出來囉～", "好個屁今天又不是星期五");
        String text = (String) RandomUtils.randomElement(list);
        return new TextMessage(text);
    }
}
