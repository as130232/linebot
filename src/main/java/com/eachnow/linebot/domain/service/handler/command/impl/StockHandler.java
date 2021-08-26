package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.twse.IndexPO;
import com.eachnow.linebot.common.po.twse.RatioAndDividendYieldPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.TwseApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Command({"stock", "股", "股票", "殖利率", "淨值", "本益比"})
public class StockHandler implements CommandHandler {
    private TwseApiService twseApiService;
    private String UP_ARROW_URL = "https://i.imgur.com/JExyVSt.png";
    private String DOWN_ARROW_URL = "https://i.imgur.com/NoekYfY.png";

    @Autowired
    public StockHandler(TwseApiService twseApiService) {
        this.twseApiService = twseApiService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        //指數，取得大盤及各類指數
        if (text.contains("指數") || text.contains("大盤")) {
            return this.getIndex(commandPO);
            //取得最新(昨日)個股本益比、殖利率及股價淨值比
        } else if (Arrays.asList("殖利率", "淨值", "本益比").contains(commandPO.getCommand())) {
            return this.getRatioAndDividendYield(commandPO);
        }
        //Todo 紀錄該股並自動換算停利停損價格

        return null;
    }

    private Message getIndex(CommandPO commandPO) {
        String date = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        boolean isCurrentDate = false;
        if (date == null) {
            isCurrentDate = true;
            date = DateUtils.getCurrentDate(DateUtils.yyyyMMdd);
        }
        //判斷是否有datetimepicker
        if (commandPO.getDatetimepicker() != null && commandPO.getDatetimepicker().getDate() != null)
            date = DateUtils.parseDate(commandPO.getDatetimepicker().getDate(), DateUtils.yyyyMMddDash, DateUtils.yyyyMMdd);

        IndexPO twIndex = twseApiService.getDailyTradingOfTaiwanIndex(date);
        ZonedDateTime parseDate = DateUtils.parseDate(date, DateUtils.yyyyMMdd);
        if (isCurrentDate && ZonedDateTime.now(DateUtils.CST_ZONE_ID).getHour() < 14) {
            return new TextMessage("今日台股還未關市。請於14:00後查詢。");
        }
        //取得該日期對應星期幾
        String dayOfWeekName = parseDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.TAIWAN).replace("星期", "");
        if (twIndex == null) {
            return new TextMessage(parseDate.format(DateUtils.yyyyMMddDash) + "(" + dayOfWeekName + ") 台股未開市。");
        }
        List<IndexPO> listCategoryIndex = twseApiService.getDailyTradeSummaryOfAllIndex(date);
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(
                Text.builder().text("台股各類指數日成交量").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).margin(FlexMarginSize.SM).color("#ffffff").align(FlexAlign.CENTER).build()
        )).paddingAll(FlexPaddingSize.MD).backgroundColor("#FF6B6E").build();

        //Title
        Box title = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.SM).spacing(FlexMarginSize.SM).contents(
                Text.builder().text("指數名稱").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).build(),
                Text.builder().text("金額(億)").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build(),
                Text.builder().text("數量(筆)").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build(),
                Text.builder().text("漲跌(%)").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build()
        ).build();
        Separator separator = Separator.builder().margin(FlexMarginSize.MD).color("#666666").build();

        List<FlexComponent> bodyComponent = new ArrayList<>();
        bodyComponent.add(Box.builder().layout(FlexLayout.VERTICAL).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).contents(
                title, separator).build());
        List<FlexComponent> listCategoryIndexComponent = listCategoryIndex.stream().map(po -> Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                Text.builder().text(po.getName().length() > 4 ? po.getName().substring(0, 4) : po.getName()).size(FlexFontSize.SM).flex(1).build(),
                Text.builder().text(po.getTradeValue()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END).build(),
                Text.builder().text(po.getTransaction()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END).build(),
                Text.builder().text(po.getChange().toString()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END)
                        .color(po.getChange().toString().contains("-") ? "#228b22" : "#ff0000").build()
        )).build()).collect(Collectors.toList());
        bodyComponent.addAll(listCategoryIndexComponent);
        bodyComponent.add(separator);
        Box twIndexBox = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                Text.builder().text("加權指").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#555555").build(),
                Text.builder().text(twIndex.getTradeValue()).size(FlexFontSize.SM).align(FlexAlign.END).build(),
                Text.builder().text(twIndex.getTransaction()).size(FlexFontSize.SM).align(FlexAlign.END).build(),
                Text.builder().text(twIndex.getChange().toString()).size(FlexFontSize.SM).align(FlexAlign.END)
                        .color(twIndex.getChange().toString().contains("-") ? "#228b22" : "#ff0000").build())).build();
        bodyComponent.add(twIndexBox);

        String datetimepickerData = "股票 指數 " + date + ParamterUtils.CONTACT;
        Box twIndexAndDateBox = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                Button.builder().height(Button.ButtonHeight.SMALL).style(Button.ButtonStyle.SECONDARY).action(
                        DatetimePickerAction.OfLocalDate.builder().data(datetimepickerData + "datetimepicker")
                                .label(DateUtils.parseDate(date, DateUtils.yyyyMMdd, DateUtils.yyyyMMddSlash) + "(" + dayOfWeekName + ")").build()).build(),
                Text.builder().text(twIndex.getTaiex()).size(FlexFontSize.XL).align(FlexAlign.CENTER).gravity(FlexGravity.CENTER)
                        .color(twIndex.getChange().toString().contains("-") ? "#228b22" : "#ff0000").build())).build();
        bodyComponent.add(twIndexAndDateBox);
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.NONE).build();

        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(null).build();
        return FlexMessage.builder().altText("台股各類指數日成交量").contents(contents).build();
    }

    /**
     * 取得個股本益比、殖利率及股價淨值比(日期查詢)
     */
    private Message getRatioAndDividendYield(CommandPO commandPO) {
        String sortCondition = null;
        if (commandPO.getText().contains("%SORT")) {    //取得排序條件，並重新組合text字串與params
            String[] textArr = commandPO.getText().split("|SORT");
            sortCondition = textArr[1];
            commandPO.setText(textArr[0]);
            commandPO.setParams(ParamterUtils.listParameter(textArr[0]));
        }
        List<String> params = commandPO.getParams();
        String date = DateUtils.yyyyMMdd.format(Instant.now().minus(1, ChronoUnit.DAYS));
        List<RatioAndDividendYieldPO> list = twseApiService.getRatioAndDividendYield(date);
        //篩選個股
        if (params.size() > 0) {
            Set<String> codes = new HashSet<>();
            Set<String> names = new HashSet<>();
            for (String param : params) {
                if (NumberUtils.isNumber(param)) {
                    codes.add(param);
                } else
                    names.add(param);
            }
            list = list.stream().filter(po -> codes.contains(po.getCode()) || names.contains(po.getName())).collect(Collectors.toList());
        }

        //排序，預設殖利率
        Comparator<RatioAndDividendYieldPO> comparator = Comparator.comparing(RatioAndDividendYieldPO::getDividendYield).reversed();
        if (sortCondition != null) {
            if (commandPO.getText().contains("_PRICE")) {
                comparator = Comparator.comparing(RatioAndDividendYieldPO::getPrice);
            } else if (commandPO.getText().contains("_PE")) {
                comparator = Comparator.comparing(RatioAndDividendYieldPO::getPeRatio);
            } else if (commandPO.getText().contains("_DY")) {
                comparator = Comparator.comparing(RatioAndDividendYieldPO::getDividendYield);
            }
            if (commandPO.getText().contains("_UP")) {
                comparator = comparator.reversed();
            }
        }
        list = list.stream().sorted(comparator).collect(Collectors.toList());

        //只顯示30筆資訊
        int MAX_SIZE = 30;
        if (list.size() > MAX_SIZE)
            list = list.stream().limit(MAX_SIZE).collect(Collectors.toList());

        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(
                Text.builder().text("個股淨值、本益比、殖利率").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).margin(FlexMarginSize.SM).color("#ffffff").align(FlexAlign.CENTER).build()
        )).paddingAll(FlexPaddingSize.MD).backgroundColor("#FF527A").build();
        //Title
        Box title = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).contents(
                Text.builder().text("代號 名稱").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(2).align(FlexAlign.START).build(),
                Text.builder().text("股價").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).build(),
                Text.builder().text("淨值").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).build(),
                Text.builder().text("本益比").size(FlexFontSize.SM).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).gravity(FlexGravity.CENTER).build(),
                Text.builder().text("殖利率").size(FlexFontSize.SM).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).gravity(FlexGravity.CENTER).build()
        ).build();
        Separator separator = Separator.builder().margin(FlexMarginSize.MD).color("#666666").build();

        List<FlexComponent> bodyComponent = new ArrayList<>();
        bodyComponent.add(Box.builder().layout(FlexLayout.VERTICAL).margin(FlexMarginSize.NONE).spacing(FlexMarginSize.SM).contents(
                title, separator).build());

        List<FlexComponent> listComponent = list.stream().map(po -> {
            //取得股價
            String priceColor = Double.valueOf(po.getPrice()).compareTo(Double.valueOf(po.getAvePrice())) > 0 ? "#ff0000" : "#228b22";
            return Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                    Text.builder().text(po.getCode() + " " + po.getName()).size(FlexFontSize.SM).flex(2).align(FlexAlign.START).build(),
                    Text.builder().text("-1".equals(po.getPrice()) ? "---" : po.getPrice()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END).color(priceColor).build(),
                    Text.builder().text(po.getPbRatio()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END).build(),
                    Text.builder().text("-1".equals(po.getPeRatio()) ? "---" : po.getPeRatio()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END).build(),
                    Text.builder().text(po.getDividendYield()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END)
                            .color(Double.valueOf(po.getDividendYield()).compareTo(8d) > 0 ? "#ff0000" : "#111111").build()
            )).build();
        }).collect(Collectors.toList());
        bodyComponent.addAll(listComponent);
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.NONE).build();

        Box priceButtonBox = Box.builder().layout(FlexLayout.BASELINE).contents(
                Text.builder().text("▲").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + "%SORT_PRICE_UP").build()).build(),
                Text.builder().text("股 價").size(FlexFontSize.Md).flex(0).weight(Text.TextWeight.BOLD).color("#ffffff").build(),
                Text.builder().text("▼").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + "%SORT_PRICE_DOWN").build()).build()
        ).flex(0).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).build();
        Box peRatioButtonBox = Box.builder().layout(FlexLayout.BASELINE).contents(
                Text.builder().text("▲").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + "%SORT_PE_UP").build()).build(),
                Text.builder().text("本益比").size(FlexFontSize.Md).flex(0).weight(Text.TextWeight.BOLD).color("#ffffff").build(),
                Text.builder().text("▼").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + "%SORT_PE_DOWN").build()).build()
        ).flex(0).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).build();
        Box dividendYieldButtonBox = Box.builder().layout(FlexLayout.BASELINE).contents(
                Text.builder().text("▲").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + "%SORT_DY_UP").build()).build(),
                Text.builder().text("殖利率").size(FlexFontSize.Md).flex(0).weight(Text.TextWeight.BOLD).color("#ffffff").build(),
                Text.builder().text("▼").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + "%SORT_DY_DOWN").build()).build()
        ).flex(0).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).build();

        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                priceButtonBox, peRatioButtonBox, dividendYieldButtonBox).backgroundColor("#F06886").build();

        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return FlexMessage.builder().altText("個股淨值、本益比、殖利率").contents(contents).build();
    }

//    @PostConstruct
//    private void test() {
//        String text = "股票 指數 20210501";
//        CommandPO commandPO = CommandPO.builder().userId("Uf52a57f7e6ba861c05be8837bfbcf0c6").text(text)
//                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
//        getIndex(commandPO);
//    }
}
