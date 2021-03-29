package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.CurrencyEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.CurrencyUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.CurrencyService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.linecorp.bot.model.action.PostbackAction;
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
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ROUND_HALF_DOWN;

@Slf4j
@Command({"currency", "幣", "匯率"})
public class CurrencyHandler implements CommandHandler {
    private CurrencyService currencyService;

    @Autowired
    public CurrencyHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        JSONObject jsonObject = currencyService.getCurrency();
        CurrencyEnum from = commandPO.getParams().size() > 0 ? CurrencyEnum.parse(commandPO.getParams().get(0)) : null;
        CurrencyEnum to = commandPO.getParams().size() > 1 ? CurrencyEnum.parse(commandPO.getParams().get(1)) : null;
        String amount = commandPO.getParams().size() > 2 ? commandPO.getParams().get(2) : null;
        if (!commandPO.getText().equals(commandPO.getCommand()) && to == null) {
            QuickReply quickReply = getQuickReply(commandPO.getText() + " ");
            return TextMessage.builder().text("原貨幣為: " + from.getName() + ", 請選擇欲轉換貨幣。").quickReply(quickReply).build();
        }
        if (!commandPO.getText().equals(commandPO.getCommand()) && amount == null) {
            MessageHandler.setUserAndCacheCommand(commandPO.getUserId(), commandPO.getText() + ParamterUtils.CONTACT);
            return TextMessage.builder().text("原貨幣為: " + from.getName() + ", 欲轉換貨幣為: " + to.getName() + ", 請輸入轉換金額。").build();
        }
        //轉換匯率
        if (from != null && to != null && amount != null) {
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());  //清除cacheCommand
            BigDecimal fromExrate = CurrencyUtils.getExrate(jsonObject, from.getKey());
            BigDecimal toExrate = CurrencyUtils.getExrate(jsonObject, to.getKey());
            //公式 = 1美金 除 原貨幣 乘 轉換貨幣 乘 金額(在四捨五入)
            BigDecimal result = (BigDecimal.valueOf(1l).divide(fromExrate, 4, ROUND_HALF_DOWN).multiply(toExrate).multiply(new BigDecimal(amount)));
            result = result.setScale(2, BigDecimal.ROUND_HALF_UP);
            return TextMessage.builder().text(from.getName() + ":" + amount + " = " + to.getName() + ":" + result).build();
        }

        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("即時匯率").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#27ACB2").build();

        List<CurrencyEnum> listCommonCurrencyEnum = CurrencyEnum.commonCurrency();
        List<FlexComponent> bodyContents = new ArrayList<>();
        Text usdText = Text.builder().text("1 美金(USD) = ").size(FlexFontSize.SM).weight(Text.TextWeight.BOLD).offsetBottom(FlexOffsetSize.SM).build();
        bodyContents.add(usdText);
        listCommonCurrencyEnum.stream().forEach(currencyEnum -> {
            BigDecimal exrate = CurrencyUtils.getExrate(jsonObject, currencyEnum.getKey());
            List<FlexComponent> currencyContents = Arrays.asList(
                    //幣值名稱
                    Text.builder().text(currencyEnum.getName() + "({code})".replace("{code}", currencyEnum.toString()))
                            .size(FlexFontSize.SM).color("#555555").flex(0).build(),
                    //匯率
                    Text.builder().text("${exrate}".replace("{exrate}", exrate.toString())).size(FlexFontSize.SM).color("#111111").align(FlexAlign.END).build()
            );
            Box currencyBody = Box.builder().layout(FlexLayout.HORIZONTAL).contents(currencyContents).margin(FlexMarginSize.SM).build();
            bodyContents.add(currencyBody);
            Separator separator = Separator.builder().margin(FlexMarginSize.XS).color("#666666").build();
            bodyContents.add(separator);

        });
        //最後更新時間
        String utc = CurrencyUtils.getUtc(jsonObject, CurrencyEnum.TWD.getKey());
        Text utcText = Text.builder().text(utc).size(FlexFontSize.XS).style(Text.TextStyle.ITALIC).weight(Text.TextWeight.REGULAR).align(FlexAlign.END).margin(FlexMarginSize.XS).offsetTop(FlexOffsetSize.MD).build();
        bodyContents.add(utcText);
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.XL).build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(null).build();
        String data = this.getClass().getAnnotation(Command.class).value()[0] + ParamterUtils.CONTACT;  //取得該指令，讓Postback再回到該command handler
        QuickReply quickReply = getQuickReply(data);
        return FlexMessage.builder().altText("Currency即時匯率").contents(contents).quickReply(quickReply).build();
    }


    private QuickReply getQuickReply(String data) {
        List<QuickReplyItem> items = CurrencyEnum.listCurrencyForQuickReply().stream().map(currencyEnum -> {
            return QuickReplyItem.builder().action(PostbackAction.builder().label(currencyEnum.getName())
                    .data(data + currencyEnum.getName()).build()).build();
        }).collect(Collectors.toList());
        QuickReply quickReply = QuickReply.builder().items(items).build();
        return quickReply;
    }

    private String getFormat() {
        return "貨幣 {原始幣值} {轉換幣值} {金額}";
    }

}
