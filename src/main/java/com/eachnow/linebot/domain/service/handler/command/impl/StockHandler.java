package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.twse.IndexPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.TwseApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Command({"stock", "股", "股票"})
public class StockHandler implements CommandHandler {
    private TwseApiService twseApiService;

    @Autowired
    public StockHandler(TwseApiService twseApiService) {
        this.twseApiService = twseApiService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        //Todo 紀錄該股並自動換算停利停損價格

        //健檢該檔本益比、成長率、

        //指數，取得大盤及各類指數
        if (text.contains("指數") || text.contains("大盤")) {
            return this.getIndex(commandPO);
        }
        return null;
    }

    private Message getIndex(CommandPO commandPO) {
        String date = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        if (date == null)
            date = DateUtils.getCurrentDate(DateUtils.yyyyMMdd);
        //判斷是否有datetimepicker
        if (commandPO.getDatetimepicker() != null && commandPO.getDatetimepicker().getDate() != null)
            date = commandPO.getDatetimepicker().getDate();

        IndexPO twIndex = twseApiService.getDailyTradingOfTaiwanIndex(date);
        List<IndexPO> listCategoryIndex = twseApiService.getDailyTradeSummaryOfAllIndex(date);

        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(Arrays.asList(
                Text.builder().text("台股各類指數日成交量").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).margin(FlexMarginSize.SM).color("#ffffff").align(FlexAlign.CENTER).build()
        )).paddingAll(FlexPaddingSize.XS).backgroundColor("#FF6B6E").build();

        //Title
        Box title = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).contents(
                Text.builder().text("指數名稱").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).build(),
                Text.builder().text("金額(萬)").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build(),
                Text.builder().text("筆數").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build(),
                Text.builder().text("漲跌").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build()
        ).build();
        Separator separator = Separator.builder().margin(FlexMarginSize.MD).color("#666666").build();

        List<FlexComponent> bodyComponent = new ArrayList<>();
        bodyComponent.add(Box.builder().layout(FlexLayout.VERTICAL).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).contents(
                title, separator).build());
        List<FlexComponent> listCategoryIndexComponent = listCategoryIndex.stream().map(po -> {
            return Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                    Text.builder().text(po.getName()).size(FlexFontSize.SM).flex(1).build(),
                    Text.builder().text(po.getTradeValue()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END).build(),
                    Text.builder().text(po.getTransaction()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END).build(),
                    Text.builder().text(po.getChange().toString()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END)
                            .color(po.getChange().toString().contains("-") ? "#228b22" : "#ff0000").build()
            )).build();
        }).collect(Collectors.toList());
        bodyComponent.addAll(listCategoryIndexComponent);
        bodyComponent.add(separator);
        Box twIndexBox = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                Text.builder().text("台指").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#555555").build(),
                Text.builder().text(twIndex.getTradeValue()).size(FlexFontSize.SM).align(FlexAlign.END).build(),
                Text.builder().text(twIndex.getTransaction()).size(FlexFontSize.SM).align(FlexAlign.END).build(),
                Text.builder().text(twIndex.getChange().toString()).size(FlexFontSize.SM).align(FlexAlign.END)
                        .color(twIndex.getChange().toString().contains("-") ? "#228b22" : "#ff0000").build())).build();
        bodyComponent.add(twIndexBox);

        String datetimepickerData = "股票 指數 " + date + ParamterUtils.CONTACT;
        Box twIndexAndDateBox = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                Button.builder().height(Button.ButtonHeight.SMALL).style(Button.ButtonStyle.SECONDARY).action(
                        DatetimePickerAction.OfLocalDate.builder().data(datetimepickerData + "datetimepicker").label(DateUtils.parseDate(date, DateUtils.yyyyMMdd, DateUtils.yyyyMMddSlash)).build()).build(),
                Text.builder().text(twIndex.getTaiex().toString()).size(FlexFontSize.XL).align(FlexAlign.CENTER).gravity(FlexGravity.CENTER)
                        .color(twIndex.getChange().toString().contains("-") ? "#228b22" : "#ff0000").build())).build();
        bodyComponent.add(twIndexAndDateBox);
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.NONE).build();

        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(null).build();
        return FlexMessage.builder().altText("台股各類指數日成交量").contents(contents).build();
    }

//    @PostConstruct
//    private void test() {
//        String text = "股票 指數";
//        CommandPO commandPO = CommandPO.builder().userId("Uf52a57f7e6ba861c05be8837bfbcf0c6").text(text)
//                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
//        getIndex(commandPO);
//    }
}
