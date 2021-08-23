package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.PttConstant;
import com.eachnow.linebot.common.constant.WeatherElementEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.crawler.PttCrawlerService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Command({"十大"})
public class TopTenArticleHandler implements CommandHandler {
    private final ThreadPoolExecutor pttCrawlerExecutor;
    private PttCrawlerService pttCrawlerService;

    @Autowired
    public TopTenArticleHandler(@Qualifier("ptt-crawler-executor") ThreadPoolExecutor pttCrawlerExecutor,
                                PttCrawlerService pttCrawlerService) {
        this.pttCrawlerExecutor = pttCrawlerExecutor;
        this.pttCrawlerService = pttCrawlerService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> params = commandPO.getParams();
        String locationName = ParamterUtils.getValueByIndex(params, 0);
        String elementName = WeatherElementEnum.getElement(ParamterUtils.getValueByIndex(params, 1));
        String url = PttConstant.GOSSIPING_URL;

//        List<String> result = pttCrawlerService.crawler(url, 5);


        return null;
    }

}
