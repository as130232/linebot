package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.crawler.ActressCrawlerService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Command({"stock", "股票"})
public class StockHandler implements CommandHandler {

    @Autowired
    public StockHandler() {
    }

    @Override
    public Message execute(CommandPO commandPO) {
        return null;
    }

}
