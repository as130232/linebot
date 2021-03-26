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
@Command({"hi", "hello", "嗨", "你好", "妳好", "哈囉"})
public class HelloHandler implements CommandHandler {

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> list = Arrays.asList("Hello", "哈囉", "安安", "你好呀魯蛇", "別吵我還沒睡醒",
                "你是天線寶寶嗎？太陽出來囉～", "好個屁今天又不是星期五");
        String text = (String) RandomUtils.randomElement(list);
        return new TextMessage(text);
    }
}
