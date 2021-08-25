package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.PttEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.PttArticlePO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.crawler.PttCrawlerService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Command({"文章", "熱門", "topten"})
public class PttArticleHandler implements CommandHandler {
    private final ThreadPoolExecutor pttCrawlerExecutor;
    private PttCrawlerService pttCrawlerService;

    @Autowired
    public PttArticleHandler(@Qualifier("ptt-crawler-executor") ThreadPoolExecutor pttCrawlerExecutor,
                             PttCrawlerService pttCrawlerService) {
        this.pttCrawlerExecutor = pttCrawlerExecutor;
        this.pttCrawlerService = pttCrawlerService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        List<String> params = commandPO.getParams();
        String name = ParamterUtils.getValueByIndex(params, 0);
        PttEnum pttEnum = PttEnum.getPttEnum(name);
        List<PttArticlePO> listPttArticle = pttCrawlerService.crawlerByDisp(pttEnum, 1);
        List<PttArticlePO> sortListPttArticle = listPttArticle.stream().sorted(Comparator.comparing(PttArticlePO::getPopularity).reversed()).collect(Collectors.toList());
        List<FlexComponent> bodyComponent = new ArrayList<>();
        for (PttArticlePO pttArticlePO : sortListPttArticle) {
            URI uri = URI.create(pttArticlePO.getWebUrl());
            Text title = Text.builder().text(pttArticlePO.getTitle()).size(FlexFontSize.SM).color("#99ccff").wrap(true).flex(5)
                    .action(new URIAction(pttEnum.toString(), uri, new URIAction.AltUri(uri))).build();
            Text popularity = Text.builder().text(pttArticlePO.getPopularity().toString()).color("#bbbbbb").align(FlexAlign.END).build();
            Box article = Box.builder().layout(FlexLayout.HORIZONTAL).contents(Arrays.asList(
                    title, popularity)).backgroundColor("#111111").paddingBottom(FlexPaddingSize.MD).build();
            bodyComponent.add(article);
        }
        Box content = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.SM)
                .margin(FlexMarginSize.MD).build();
        URI uri = URI.create(PttEnum.getUrlByDisp(pttEnum));
        Text pttName = Text.builder().text(pttEnum.toString()).size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).color("#1DB446").align(FlexAlign.CENTER)
                .action(new URIAction(pttEnum.toString(), uri, new URIAction.AltUri(uri))).build();
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(Arrays.asList(pttName, content)).backgroundColor("#03031B")
                .paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.LG).paddingBottom(FlexPaddingSize.LG).build();
        FlexContainer contents = Bubble.builder().header(null).hero(null).body(body).footer(null).build();
        return FlexMessage.builder().altText(pttEnum.getName() + "版熱門文章").contents(contents).build();
    }

}
