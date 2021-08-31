package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.javdb.ArticlePO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.crawler.JavdbCrawlerService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Command({"av"})
public class JavdbHandler implements CommandHandler {
    private JavdbCrawlerService javdbCrawlerService;

    @Autowired
    private JavdbHandler(JavdbCrawlerService javdbCrawlerService) {
        this.javdbCrawlerService = javdbCrawlerService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String type = ParamterUtils.getValueByIndex(commandPO.getParams(), 0);
        if (type == null)
            type = JavdbCrawlerService.TYPE_DAILY;
        List<ArticlePO> list = javdbCrawlerService.getArticle(type);
        //只留12筆
        list = list.stream().limit(12).collect(Collectors.toList());
        List<Bubble> listBubble = list.stream().map(po -> {
            URI uri = URI.create(po.getWebUrl());
            URI pictureUri = URI.create(po.getPictureUrl());
            Image image = Image.builder().size(Image.ImageSize.FULL_WIDTH).aspectMode(Image.ImageAspectMode.Cover).aspectRatio(5, 7)
                    .url(pictureUri).action(new URIAction("Picture URL", pictureUri, new URIAction.AltUri(uri))).build();
            Box codeAndDate = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                    Text.builder().text(po.getCode()).color("#1DB446").size(FlexFontSize.LG).flex(2).weight(Text.TextWeight.BOLD).build(),
                    Text.builder().text(po.getDate()).color("#ffffff").size(FlexFontSize.XS).flex(0).align(FlexAlign.END).gravity(FlexGravity.BOTTOM).style(Text.TextStyle.ITALIC).build()
            ).build();
            Box title = Box.builder().layout(FlexLayout.VERTICAL).contents(Text.builder().text(po.getTitle()).color("#ebebeb").size(FlexFontSize.XS).wrap(true).flex(0).build())
                    .spacing(FlexMarginSize.LG).build();
            Box content = Box.builder().layout(FlexLayout.VERTICAL).contents(codeAndDate, title).backgroundColor("#03303Acc").position(FlexPosition.ABSOLUTE)
                    .offsetBottom("0px").offsetStart("0px").offsetEnd("0px").paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.XS).build();
            List<FlexComponent> bodyContents = Arrays.asList(image, content);
            Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.NONE).build();
            Bubble bubble = Bubble.builder().header(null).hero(null).body(body).footer(null)
                    .size(Bubble.BubbleSize.KILO).direction(FlexDirection.LTR).action(new URIAction("Web URL", uri, new URIAction.AltUri(uri))).build();
            return bubble;
        }).collect(Collectors.toList());
        FlexContainer contents = Carousel.builder().contents(listBubble).build();
        return new FlexMessage("排行榜-" + javdbCrawlerService.getTypeName(type), contents);
    }


}
