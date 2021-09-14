package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.opendata.CpcOilPricePO;
import com.eachnow.linebot.domain.service.gateway.CpcOilApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Separator;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Command({"中油", "油價"})
public class OilPriceHandler implements CommandHandler {
    private CpcOilApiService cpcOilApiService;
    private SAXReader saxReader;

    @Autowired
    public OilPriceHandler(CpcOilApiService cpcOilApiService,
                           SAXReader saxReader) {
        this.cpcOilApiService = cpcOilApiService;
        this.saxReader = saxReader;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        CpcOilPricePO cpcOilPricePO = cpcOilApiService.getOilPrice();
        if (cpcOilPricePO == null)
            return new TextMessage("找無資料");
        String content = "";
        try {
            Document document = saxReader.read(new ByteArrayInputStream(cpcOilPricePO.getUpOrDownHtml().getBytes(StandardCharsets.UTF_8)));
            Element root = document.getRootElement();
            for (Element element : root.elements()) {
                if (element.isTextOnly()) {
                    content += element.getText() + " ";
                } else {
                    content += element.element("i").getText();
                }
            }
        } catch (DocumentException e) {
        }
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(
                Text.builder().text("中油即時油價").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).margin(FlexMarginSize.SM).color("#ffffff").align(FlexAlign.CENTER).build()
        )).paddingAll(FlexPaddingSize.MD).backgroundColor("#1889D6").build();

        List<FlexComponent> bodyComponent = new ArrayList<>();
        //本週汽油調整
        Box adjustmentThisWeek = Box.builder().layout(FlexLayout.VERTICAL).contents(
                Text.builder().text(content).color("#ffffff").size(FlexFontSize.XL).align(FlexAlign.CENTER).gravity(FlexGravity.CENTER).margin(FlexMarginSize.SM).build()
        ).backgroundColor("#039920").margin(FlexMarginSize.MD).cornerRadius(FlexCornerRadiusSize.MD).build();
        Separator separator = Separator.builder().margin(FlexMarginSize.SM).build();
        Box title1 = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                Text.builder().text("92無鉛").flex(1).size(FlexFontSize.Md).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text("95無鉛").flex(1).size(FlexFontSize.Md).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text("98無鉛").flex(1).size(FlexFontSize.Md).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build()
        ).margin(FlexMarginSize.SM).build();
        Box price1 = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                Text.builder().text(cpcOilPricePO.getPrice92()).flex(1).color("#e46a4a").size(FlexFontSize.XL).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text(cpcOilPricePO.getPrice95()).flex(1).color("#e46a4a").size(FlexFontSize.XL).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text(cpcOilPricePO.getPrice98()).flex(1).color("#e46a4a").size(FlexFontSize.XL).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build()
        ).margin(FlexMarginSize.SM).build();
        Box title2 = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                Text.builder().text("酒精汽油").flex(1).size(FlexFontSize.Md).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text("超級柴油").flex(1).size(FlexFontSize.Md).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text("液化石油氣").flex(1).size(FlexFontSize.Md).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build()
        ).margin(FlexMarginSize.SM).build();
        Box price2 = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                Text.builder().text(cpcOilPricePO.getPriceAlcohol()).flex(1).color("#e46a4a").size(FlexFontSize.XL).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text(cpcOilPricePO.getPriceDiesel()).flex(1).color("#e46a4a").size(FlexFontSize.XL).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text(cpcOilPricePO.getPriceLpg()).flex(1).color("#e46a4a").size(FlexFontSize.XL).align(FlexAlign.CENTER).margin(FlexMarginSize.SM).weight(Text.TextWeight.BOLD).build()
        ).margin(FlexMarginSize.SM).build();
        bodyComponent.addAll(Arrays.asList(adjustmentThisWeek, separator, title1, price1, title2, price2));

        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.NONE).build();
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(
                Text.builder().text("資料更新於 " + cpcOilPricePO.getPriceUpdate()).style(Text.TextStyle.ITALIC).align(FlexAlign.END).offsetEnd(FlexOffsetSize.SM).build()
        ).backgroundColor("#1889D6").build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return FlexMessage.builder().altText("台股各類指數日成交量").contents(contents).build();
    }
}
