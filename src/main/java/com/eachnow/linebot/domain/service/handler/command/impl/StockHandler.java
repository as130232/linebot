package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.twse.IndexPO;
import com.eachnow.linebot.common.po.twse.RatioAndDividendYieldPO;
import com.eachnow.linebot.common.po.twse.TradeValueInfoPO;
import com.eachnow.linebot.common.po.twse.TradeValuePO;
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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Command({"stock", "股", "股票",
        "股價", "殖利率", "淨值", "本益比",
        "三大法人", "融資融卷"})
public class StockHandler implements CommandHandler {
    private TwseApiService twseApiService;

    @Autowired
    public StockHandler(TwseApiService twseApiService) {
        this.twseApiService = twseApiService;
    }

//        @PostConstruct
//    private void test() {
//        String text = "三大法人 日報 20210827";
//        CommandPO commandPO = CommandPO.builder().userId("Uf52a57f7e6ba861c05be8837bfbcf0c6").text(text)
//                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
//        execute(commandPO);
//    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        //指數，取得大盤及各類指數
        if (text.contains("指數") || text.contains("大盤")) {
            return this.getIndex(commandPO);
            //取得最新(昨日)個股本益比、殖利率及股價淨值比
        } else if (Arrays.asList("股價", "殖利率", "淨值", "本益比").contains(commandPO.getCommand())) {
            return this.getRatioAndDividendYield(commandPO);
        } else if (text.contains("三大法人")) {
            return this.getTradingOfForeignAndInvestors(commandPO);
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
        Separator separator = Separator.builder().margin(FlexMarginSize.SM).color("#666666").build();

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
        if (commandPO.getText().contains(" %SORT")) {    //取得排序條件，並重新組合text字串與params
            String[] textArr = commandPO.getText().split(" %SORT");
            sortCondition = textArr[1];
            commandPO.setText(textArr[0]);
            commandPO.setParams(ParamterUtils.listParameter(textArr[0]));
        }
        String date = DateUtils.yyyyMMdd.format(Instant.now().minus(1, ChronoUnit.DAYS));
        List<String> params = commandPO.getParams();
        String dateOrStockCode = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);   //第一個參數有可能為日期或股票代號
        if (dateOrStockCode != null && dateOrStockCode.length() == 8) { //若長度為8則為日期(20210101)
            date = dateOrStockCode;
            params = params.stream().filter(param -> !param.equals(dateOrStockCode)).collect(Collectors.toList());
        }

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
            if (sortCondition.contains("_PRICE")) {
                comparator = Comparator.comparing(RatioAndDividendYieldPO::getPrice);
            } else if (sortCondition.contains("_PE")) {
                comparator = Comparator.comparing(RatioAndDividendYieldPO::getPeRatio);
            } else if (sortCondition.contains("_DY")) {
                comparator = Comparator.comparing(RatioAndDividendYieldPO::getDividendYield);
            }
            if (sortCondition.contains("_UP")) {
                comparator = comparator.reversed();
            }
        }
        list = list.stream().sorted(comparator).collect(Collectors.toList());

        //只顯示40筆資訊
        int MAX_SIZE = 40;
        if (list.size() > MAX_SIZE)
            list = list.stream().limit(MAX_SIZE).collect(Collectors.toList());

        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(
                Text.builder().text("個股淨值、本益比、殖利率").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).margin(FlexMarginSize.SM).color("#ffffff").align(FlexAlign.CENTER).build()
        )).paddingAll(FlexPaddingSize.MD).backgroundColor("#FF527A").build();
        //Title
        Box title = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).contents(
                Text.builder().text("代號 名稱").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(2).align(FlexAlign.START).build(),
                Text.builder().text("股價").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).build(),
                Text.builder().text("淨值").size(FlexFontSize.SM).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).gravity(FlexGravity.CENTER).build(),
                Text.builder().text("本益比").size(FlexFontSize.SM).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).gravity(FlexGravity.CENTER).build(),
                Text.builder().text("殖利率").size(FlexFontSize.SM).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.END).gravity(FlexGravity.CENTER).build()
        ).build();
        Separator separator = Separator.builder().margin(FlexMarginSize.SM).color("#666666").build();

        List<FlexComponent> bodyComponent = new ArrayList<>();
        bodyComponent.add(Box.builder().layout(FlexLayout.VERTICAL).margin(FlexMarginSize.NONE).spacing(FlexMarginSize.SM).contents(
                title, separator).build());

        List<FlexComponent> listComponent = list.stream().map(po -> {
            String priceColor = po.getPrice().compareTo(po.getAvePrice()) > 0 ? "#ff0000" : "#228b22";
            return Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                    Text.builder().text(po.getCode() + " " + po.getName()).size(FlexFontSize.SM).flex(2).align(FlexAlign.START).build(),
                    Text.builder().text(po.getPrice() == -1d ? "---" : po.getPrice().toString()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END).color(priceColor).build(),
                    Text.builder().text(po.getPbRatio().toString()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END).build(),
                    Text.builder().text(po.getPeRatio() == -1d ? "---" : po.getPeRatio().toString()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END)
                            .color(po.getPeRatio().compareTo(12d) < 0 ? "#ff0000" : "#111111").build(),
                    Text.builder().text(po.getDividendYield().toString()).size(FlexFontSize.SM).flex(1).align(FlexAlign.END)
                            .color(po.getDividendYield().compareTo(8d) > 0 ? "#ff0000" : "#111111").build()
            )).build();
        }).collect(Collectors.toList());
        bodyComponent.addAll(listComponent);
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.NONE).build();

        Box priceButtonBox = Box.builder().layout(FlexLayout.BASELINE).contents(
                Text.builder().text("▲").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + " %SORT_PRICE_UP").build()).build(),
                Text.builder().text("股　價").size(FlexFontSize.Md).flex(0).weight(Text.TextWeight.BOLD).color("#ffffff").build(),
                Text.builder().text("▼").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + " %SORT_PRICE_DOWN").build()).build()
        ).flex(1).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).build();
        Box peRatioButtonBox = Box.builder().layout(FlexLayout.BASELINE).contents(
                Text.builder().text("▲").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + " %SORT_PE_UP").build()).build(),
                Text.builder().text("本益比").size(FlexFontSize.Md).flex(0).weight(Text.TextWeight.BOLD).color("#ffffff").build(),
                Text.builder().text("▼").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + " %SORT_PE_DOWN").build()).build()
        ).flex(1).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).build();
        Box dividendYieldButtonBox = Box.builder().layout(FlexLayout.BASELINE).contents(
                Text.builder().text("▲").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + " %SORT_DY_UP").build()).build(),
                Text.builder().text("殖利率").size(FlexFontSize.Md).flex(0).weight(Text.TextWeight.BOLD).color("#ffffff").build(),
                Text.builder().text("▼").color("#AAF1E1").flex(0).action(PostbackAction.builder().data(commandPO.getText() + " %SORT_DY_DOWN").build()).build()
        ).flex(1).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).build();

        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                priceButtonBox, peRatioButtonBox, dividendYieldButtonBox).backgroundColor("#F06886").build();

        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return FlexMessage.builder().altText("個股股價、淨值、本益比、殖利率").contents(contents).build();
    }

    public Message getTradingOfForeignAndInvestors(CommandPO commandPO) {
        String type = ParamterUtils.getValueByIndex(commandPO.getParams(), 0);
        String date = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        //預設日報
        if (type == null)
            type = "日報";

        String preDate = null;
        if (commandPO.getDatetimepicker() != null && commandPO.getDatetimepicker().getDate() != null)
            date = DateUtils.parseDate(commandPO.getDatetimepicker().getDate(), DateUtils.yyyyMMddDash, DateUtils.yyyyMMdd);
        //預設取得昨日，若時間已經超過當天晚上七點，則取得當天日期
        if (date == null)
            date = DateUtils.getCurrentDate(DateUtils.yyyyMMdd);
        LocalDate localDate = LocalDate.parse(date, DateUtils.yyyyMMdd);
        boolean isDayType = false;
        boolean isWeekType = false;
        boolean isMonthType = false;
        switch (type) {
            case "日報": {
                isDayType = true;
                int minusDay = 1;
                //若當天是星期一，則前一天交易日為三天前(上週五)
                if (localDate.getDayOfWeek().getValue() == 1)
                    minusDay = 3;
                preDate = localDate.minus(minusDay, ChronoUnit.DAYS).format(DateUtils.yyyyMMdd);
                break;
            }
            case "週報": {    //預設取得當週一
                isWeekType = true;
                LocalDate monday = localDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                LocalDate preMonday = monday.minus(7, ChronoUnit.DAYS);
                date = monday.format(DateUtils.yyyyMMdd);
                preDate = preMonday.format(DateUtils.yyyyMMdd);
                break;
            }
            case "月報": {   //預設取得當月一號
                isMonthType = true;
                LocalDate firstOfMonth = LocalDate.of(localDate.getYear(), localDate.getMonth(), 1);
                LocalDate preFirstOfMonth = firstOfMonth.minus(1, ChronoUnit.MONTHS);
                date = firstOfMonth.format(DateUtils.yyyyMMdd);
                preDate = preFirstOfMonth.format(DateUtils.yyyyMMdd);
                break;
            }
        }
        TradeValueInfoPO tradeValuePO = twseApiService.getTradingOfForeignAndInvestors(parseDateType(type), date);
        if (tradeValuePO == null)
            return new TextMessage("查無該日期資訊");
        List<TradeValuePO> list = tradeValuePO.getTradeValues();
        TradeValueInfoPO preTradeValuePO = twseApiService.getTradingOfForeignAndInvestors(parseDateType(type), preDate);
        Map<String, TradeValuePO> preDateMap = preTradeValuePO.getTradeValues().stream().collect(Collectors.toMap(TradeValuePO::getItem, Function.identity()));

        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(Collections.singletonList(
                Text.builder().text("三大法人買賣統計").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).margin(FlexMarginSize.SM).color("#ffffff").align(FlexAlign.CENTER).build()
        )).paddingAll(FlexPaddingSize.MD).backgroundColor("#e46a4a").build();

        //Title
        Box title = Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).spacing(FlexMarginSize.SM).contents(
                Text.builder().text("(億)").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.CENTER).build(),
                Text.builder().text("買進").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.CENTER).build(),
                Text.builder().text("賣出").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.CENTER).build(),
                Text.builder().text("現貨差").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.CENTER).build(),
                Text.builder().text("昨日差").size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").flex(1).align(FlexAlign.CENTER).build()
        ).build();
        Separator separator = Separator.builder().margin(FlexMarginSize.SM).color("#666666").build();

        List<FlexComponent> bodyComponent = new ArrayList<>();
        bodyComponent.add(Box.builder().layout(FlexLayout.VERTICAL).margin(FlexMarginSize.NONE).spacing(FlexMarginSize.SM).contents(
                title, separator).build());
        List<FlexComponent> listComponent = list.stream().map(po -> {
            TradeValuePO preDatePO = preDateMap.get(po.getItem());
            Double differenceOfPreDate = po.getDifference() - preDatePO.getDifference();
            String difference = convertTradeValue(po.getDifference());
            return Box.builder().layout(FlexLayout.HORIZONTAL).margin(FlexMarginSize.MD).contents(Arrays.asList(
                    Text.builder().text(parseName(po.getItem())).size(FlexFontSize.SM).flex(1).align(FlexAlign.START).wrap(true).build(),
                    Text.builder().text(convertTradeValue(po.getTotalBuy())).size(FlexFontSize.SM).flex(1).align(FlexAlign.CENTER).build(),
                    Text.builder().text(convertTradeValue(po.getTotalSell())).size(FlexFontSize.SM).flex(1).align(FlexAlign.CENTER).build(),
                    Text.builder().text(difference.contains("-") ? difference : "+" + difference).size(FlexFontSize.SM).flex(1).align(FlexAlign.CENTER)
                            .color(po.getDifference().compareTo(-1d) > 0 ? "#ff0000" : "#228b22").build(),
                    Text.builder().text(convertTradeValue(differenceOfPreDate)).size(FlexFontSize.SM).flex(1).align(FlexAlign.CENTER)
                            .color(differenceOfPreDate.compareTo(-1d) > 0 ? "#ff0000" : "#228b22").build()
            )).build();
        }).collect(Collectors.toList());
        bodyComponent.addAll(listComponent);
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyComponent).paddingAll(FlexPaddingSize.MD).paddingTop(FlexPaddingSize.NONE).build();

        String label = parseDateLabel(tradeValuePO.getTitle());
        String datetimepickerData = commandPO.getCommand() + ParamterUtils.CONTACT + type + ParamterUtils.CONTACT + date + ParamterUtils.CONTACT;
        Box dateButton = Box.builder().layout(FlexLayout.HORIZONTAL).contents(Button.builder().flex(2).height(Button.ButtonHeight.SMALL)
                .style(Button.ButtonStyle.SECONDARY).action(DatetimePickerAction.OfLocalDate.builder()
                        .label(label).data(datetimepickerData + "datetimepicker").build()).build()
        ).build();

        String typeDate = commandPO.getCommand() + ParamterUtils.CONTACT;
        Box typeButton = Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                Button.builder().height(Button.ButtonHeight.SMALL).style(isDayType ? Button.ButtonStyle.PRIMARY : Button.ButtonStyle.LINK)
                        .action(PostbackAction.builder().label("日報").data(typeDate + "日報" + ParamterUtils.CONTACT + date).build()).build(),
                Button.builder().height(Button.ButtonHeight.SMALL).style(isWeekType ? Button.ButtonStyle.PRIMARY : Button.ButtonStyle.LINK)
                        .action(PostbackAction.builder().label("週報").data(typeDate + "週報" + ParamterUtils.CONTACT + date).build()).build(),
                Button.builder().height(Button.ButtonHeight.SMALL).style(isMonthType ? Button.ButtonStyle.PRIMARY : Button.ButtonStyle.LINK)
                        .action(PostbackAction.builder().label("月報").data(typeDate + "月報" + ParamterUtils.CONTACT + date).build()).build()
        ).build();
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(dateButton, typeButton)
                .spacing(FlexMarginSize.MD).backgroundColor("#e46a4a").build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return FlexMessage.builder().altText("三大法人買賣統計").contents(contents).build();
    }

    private String parseName(String name) {
        switch (name) {
            case "自營商(自行買賣)":
                return "自營自買";
            case "自營商(避險)":
                return "自營避險";
            case "外資及陸資(不含外資自營商)":
                return "外資";
        }
        return name;
    }

    private String parseDateLabel(String title) {
        //民國年轉西元年
        String label;
        title = title.split(" ")[0];
        String[] startAndEnd = title.split("至");
        LocalDate localStartDate = DateUtils.parseByMinguo(startAndEnd[0]);
        String startDate = localStartDate.format(DateUtils.yyyyMMddSlash);
        String dayOfWeekNameStartDate = localStartDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.TAIWAN).replace("星期", "");
        startDate += "({dayOfWeekName})".replace("{dayOfWeekName}", dayOfWeekNameStartDate);
        label = startDate;
        if (startAndEnd.length > 1) {
            LocalDate localEndDate = DateUtils.parseByMinguo(startAndEnd[1]);
            String endDate = localEndDate.format(DateUtils.yyyyMMddSlash);
            String dayOfWeekNameEndDate = localEndDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.TAIWAN).replace("星期", "");
            endDate += "({dayOfWeekName})".replace("{dayOfWeekName}", dayOfWeekNameEndDate);
            label += "－" + endDate;
        }
        return label;
    }

    private String parseDateType(String type) {
        switch (type) {
            case "日報":
                return "day";
            case "週報":
                return "week";
            case "月報":
                return "month";
        }
        return "day";   //default
    }

    /**
     * 單位從元轉為億
     */
    public String convertTradeValue(Double tradeValue) {
        BigDecimal result = (new BigDecimal(tradeValue)).divide(new BigDecimal(100000000))
                .setScale(1, BigDecimal.ROUND_HALF_UP);
        return result.toString();
    }

//    public static void main(String[] args) {
//        Instant instant = Instant.now();
//        LocalDate localDate = instant.atZone(DateUtils.CST_ZONE_ID).toLocalDate();
//        LocalDate monday = localDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
//        LocalDate preMonday = monday.minus(7, ChronoUnit.DAYS);
//        System.out.println(monday);
//        System.out.println(preMonday);
//        LocalDate firstOfMonth = LocalDate.of(localDate.getYear(), localDate.getMonth(), 1);
//        LocalDate preFirstOfMonth = firstOfMonth.minus(1, ChronoUnit.MONTHS);
//        System.out.println(firstOfMonth);
//        System.out.println(preFirstOfMonth);
//        String test = "20210823";
//        LocalDate testLocalDate = LocalDate.parse(test, DateUtils.yyyyMMdd);
//        String preDate = testLocalDate.minus(3, ChronoUnit.DAYS).format(DateUtils.yyyyMMdd);
//        System.out.println(testLocalDate.getDayOfWeek());
//    }
}
