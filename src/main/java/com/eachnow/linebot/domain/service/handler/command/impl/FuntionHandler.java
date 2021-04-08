package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DescriptionCommandPO;
import com.eachnow.linebot.common.po.DescriptionPO;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Command({"funtion", "功能"})
public class FuntionHandler implements CommandHandler {
    @Override
    public Message execute(CommandPO commandPO) {
        List<Bubble> listBubble = new ArrayList<>(10);

        //翻譯
        DescriptionPO translationDescription = TranslationHandler.getDescription();
        listBubble.add(getBubble(translationDescription));
        //記帳查帳
        DescriptionPO bookkeepingDescription = BookkeepingHandler.getDescription();
        listBubble.add(getBubble(bookkeepingDescription));
        //匯率(幣值轉換)
        DescriptionPO currencyDescription = CurrencyHandler.getDescription();
        listBubble.add(getBubble(currencyDescription));

        //表特
        DescriptionPO beautyDescription = BeautyHandler.getDescription();
        listBubble.add(getBubble(beautyDescription));
        //地點
        DescriptionPO placeDescription = PlaceHandler.getDescription();
        listBubble.add(getBubble(placeDescription));
        //天氣

        //吃什麼、你好

        //股票 待做
        FlexContainer contents = Carousel.builder().contents(listBubble).build();
        return new FlexMessage("功能列表", contents);
    }

    public Bubble getBubble(DescriptionPO descriptionPO) {
        URI imageUrl = URI.create(descriptionPO.getImageUrl() == null ? "https://i.imgur.com/R0qpw6h.jpg" : descriptionPO.getImageUrl());   //default
        FlexComponent hero = Image.builder().size(Image.ImageSize.FULL_WIDTH).aspectMode(Image.ImageAspectMode.Cover)
                .aspectRatio(320, 213).url(imageUrl).build();

        List<FlexComponent> commandContents = new ArrayList<>();
        for (int i = 0; i < descriptionPO.getCommands().size(); i++) {
            DescriptionCommandPO commandPO = descriptionPO.getCommands().get(i);
            commandContents.add(Text.builder().text((i + 1) + ". " + commandPO.getExplain()).weight(Text.TextWeight.BOLD).build());
            commandContents.add(Box.builder().layout(FlexLayout.BASELINE).contents(Arrays.asList(
                    Icon.builder().url(URI.create("https://i.imgur.com/yPacRyd.png")).size(FlexFontSize.LG).offsetTop(FlexOffsetSize.SM).offsetEnd(FlexOffsetSize.SM).build(),   //input icon
                    Text.builder().text(commandPO.getCommand()).action(PostbackAction.builder().data(commandPO.getPostback()).label(commandPO.getPostback()).build()).build()
            )).build());

            if (commandPO.getExample() != null) {
                commandContents.add(Box.builder().layout(FlexLayout.BASELINE).contents(Arrays.asList(
                        Icon.builder().url(URI.create("https://i.imgur.com/YNJ8mW7.png")).size(FlexFontSize.LG).offsetTop(FlexOffsetSize.SM).build(),   //example icon
                        Text.builder().text(commandPO.getExample()).build()
                )).build());
            }
        }
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text(descriptionPO.getDescription()).size(FlexFontSize.SM).wrap(true).build(),
                Separator.builder().color("#666666").margin(FlexMarginSize.SM).build(),
                Box.builder().layout(FlexLayout.VERTICAL).paddingTop(FlexPaddingSize.SM).contents(commandContents).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.SM).paddingAll(FlexPaddingSize.LG).contents(bodyContents).build();
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(Arrays.asList(Button.builder().height(Button.ButtonHeight.SMALL)
                .action(PostbackAction.builder().label(descriptionPO.getTitle()).data(descriptionPO.getTitle()).build())
                .style(Button.ButtonStyle.PRIMARY).color("#17c950").build())).backgroundColor("#17c950").paddingAll(FlexPaddingSize.NONE).build();
        Bubble bubble = Bubble.builder().header(null).hero(hero).body(body).footer(footer)
                .size(Bubble.BubbleSize.KILO).build();
        return bubble;
    }
}
