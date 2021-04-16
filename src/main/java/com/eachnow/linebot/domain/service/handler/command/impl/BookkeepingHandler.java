package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.CurrencyEnum;
import com.eachnow.linebot.common.db.po.BookkeepingPO;
import com.eachnow.linebot.common.db.repository.BookkeepingRepository;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DescriptionCommandPO;
import com.eachnow.linebot.common.po.DescriptionPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.*;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Command({"記", "記帳"})
public class BookkeepingHandler implements CommandHandler {
    private BookkeepingRepository bookkeepingRepository;
    private final String CONFIRM = "@bookkeepingConfirm";
    private final String CANCEL = "@bookkeepingCancel";

    @Autowired
    private BookkeepingHandler(BookkeepingRepository bookkeepingRepository) {
        this.bookkeepingRepository = bookkeepingRepository;
    }

    public static DescriptionPO getDescription() {
        List<DescriptionCommandPO> commands = new ArrayList<>();
        commands.add(DescriptionCommandPO.builder().explain("記帳").command("記 {類型} {金額} {幣值} {日期}").example("記 晚餐 100 台幣(省) 20210101(省)").postback("記 早餐 50 台").build());
        commands.add(DescriptionCommandPO.builder().explain("查帳").command("記 查 {開始時間} {結束時間}").example("記 查 20210101 20210103").postback("記 查").build());
        return DescriptionPO.builder().title("記帳").description("記帳時可在金額後輸入對應幣值，省略則為新台幣，查帳的時間格式為yyyyMMdd，省略則查詢當天日期。")
                .commands(commands).imageUrl("https://www.dummies.com/wp-content/uploads/bookkeeping-balance-sheet.jpg").build();
    }

//    @PostConstruct
//    private void test() {
//        String text = "記 查 20210401 20210409";
//        CommandPO commandPO = CommandPO.builder().userId("Uf52a57f7e6ba861c05be8837bfbcf0c6").text(text)
//                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
//        execute(commandPO);
//    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        //查帳功能
        if (text.contains("查") || text.contains("check")) {
            return this.getBookkeeper(commandPO);
        }
        String typeName = ParamterUtils.getValueByIndex(commandPO.getParams(), 0);
        String amount = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        CurrencyEnum currencyEnum = CurrencyEnum.parse(ParamterUtils.getValueByIndex(commandPO.getParams(), 2));
        String date = ParamterUtils.getValueByIndex(commandPO.getParams(), 3);
        if (date == null)
            date = DateUtils.getCurrentDate();
        if (!date.contains("-"))
            date = DateUtils.parse(date, DateUtils.yyyyMMdd, DateUtils.yyyyMMddDash);   //格式轉換，數字轉為有dash分隔
        //判斷是否有datetimepicker
        if (commandPO.getDatetimepicker() != null && commandPO.getDatetimepicker().getDate() != null)
            date = commandPO.getDatetimepicker().getDate();

        if (currencyEnum == null)
            currencyEnum = CurrencyEnum.TWD; //default 新台幣
        if (commandPO.getParams().size() < 2 || !NumberUtils.isNumber(amount)) {
            return new TextMessage("請輸入正確格式:" + getFormat() + "，例:記 晚餐 180，注意需空格隔開！");
        }
        if (text.contains(CONFIRM)) {
            BookkeepingPO po = BookkeepingPO.builder().userId(commandPO.getUserId()).typeName(typeName).amount(new BigDecimal(amount)).currency(currencyEnum.toString())
                    .date(date).createTime(DateUtils.getCurrentTime()).build();
            bookkeepingRepository.save(po);
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());    //移除緩存
            log.info("記帳成功。BookkeepingPO:{}", po);
            return new TextMessage("記帳成功。");
        } else if (text.contains(CANCEL)) {
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());    //移除緩存
            return new TextMessage("記帳已取消。");
        }
        String data = commandPO.getCommand() + ParamterUtils.CONTACT + typeName + ParamterUtils.CONTACT +
                amount + ParamterUtils.CONTACT + currencyEnum.getName() + ParamterUtils.CONTACT + date + ParamterUtils.CONTACT;
        MessageHandler.setUserAndCacheCommand(commandPO.getUserId(), data); //新增緩存
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text("類型: " + typeName).size(FlexFontSize.LG).build(),
                Text.builder().text("金額: " + amount).size(FlexFontSize.LG).build(),
                Text.builder().text("幣值: " + currencyEnum.getName()).size(FlexFontSize.LG).build(),
                Text.builder().text("日期: " + date + " (可選)").size(FlexFontSize.LG).action(DatetimePickerAction.OfLocalDate.builder()
                        .data("datetimepicker").label("選擇日期").build()).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).margin(FlexMarginSize.SM).paddingAll(FlexPaddingSize.MD).build();
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("確定").data(CONFIRM).build()).build(),
                Button.builder().style(Button.ButtonStyle.SECONDARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("取消").data(CANCEL).build()).build()
        );
        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("請問輸入正確嗎?").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#29bae6").build();
        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(footerContents).spacing(FlexMarginSize.MD).build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();
        return new FlexMessage("記帳確認", contents);
    }

    public String getFormat() {
        return "記 {類型} {金額} {幣值}(可省略)";
    }

    public Message getBookkeeper(CommandPO commandPO) {
        String startDate = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        if (startDate == null)  //預設當天
            startDate = DateUtils.getCurrentDate(DateUtils.yyyyMMdd);
        String endDate = ParamterUtils.getValueByIndex(commandPO.getParams(), 2);
        if (endDate == null)
            endDate = startDate;
        List<BookkeepingPO> listBookkeeping = bookkeepingRepository.findByUserIdAndDateBetween(commandPO.getUserId(),
                DateUtils.parse(startDate, DateUtils.yyyyMMdd, DateUtils.yyyyMMddDash),
                DateUtils.parse(endDate, DateUtils.yyyyMMdd, DateUtils.yyyyMMddDash));
        //按照日期分類
        Map<String, List<BookkeepingPO>> listBookkeepingGroupByDate = listBookkeeping.stream().collect(Collectors.groupingBy(BookkeepingPO::getDate));
        //排序，日期小的在前
        Map<String, List<BookkeepingPO>> listBookkeepingGroupByDateInSorted = listBookkeepingGroupByDate.entrySet().stream()
                .sorted((Map.Entry.comparingByKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        List<FlexComponent> bodyContents = new ArrayList<>();
        listBookkeepingGroupByDateInSorted.keySet().stream().forEach(date -> {
            List<BookkeepingPO> listBookkeepingSameDate = listBookkeepingGroupByDate.get(date);
            BigDecimal totalOneDay = listBookkeepingSameDate.stream().map(item -> item.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);
            //一天所有資訊
            List<FlexComponent> oneDateContents = new ArrayList<>();
            //取得該日期對應星期幾
            ZonedDateTime parseDate = DateUtils.parseDate(date, DateUtils.yyyyMMddDash);
            String dayOfWeekName = parseDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.TAIWAN).replace("星期", "");
            String dateContainDayOfWeek = date.replace("-", "/") + "({dayOfWeek})".replace("{dayOfWeek}", dayOfWeekName);
            Box oneDateAndTotal = Box.builder().layout(FlexLayout.HORIZONTAL).contents(Arrays.asList(
                    //日期
                    Text.builder().text(dateContainDayOfWeek).size(FlexFontSize.Md).style(Text.TextStyle.ITALIC).weight(Text.TextWeight.BOLD).color("#555555").build(),
                    //該天總金額
                    Text.builder().text("$" + totalOneDay).size(FlexFontSize.Md).weight(Text.TextWeight.BOLD).color("#111111").align(FlexAlign.END).build()
            )).paddingAll(FlexPaddingSize.XS).build();
            oneDateContents.add(oneDateAndTotal);

            listBookkeepingSameDate.stream().forEach(po -> {
                Box typeAndAmount = Box.builder().layout(FlexLayout.HORIZONTAL).contents(Arrays.asList(
                        //類型名稱
                        Text.builder().text(po.getTypeName()).size(FlexFontSize.SM).color("#555555").flex(0).offsetStart(FlexOffsetSize.MD).build(),
                        //金額
                        Text.builder().text(po.getAmount().toString() + " " + po.getCurrency()).size(FlexFontSize.SM).color("#555555").align(FlexAlign.END).build()
                )).build();
                oneDateContents.add(typeAndAmount);
            });
            Box oneDateBox = Box.builder().layout(FlexLayout.VERTICAL).contents(oneDateContents).paddingTop(FlexPaddingSize.SM).build();
            bodyContents.add(oneDateBox);
            Separator separator = Separator.builder().margin(FlexMarginSize.MD).color("#666666").build();
            bodyContents.add(separator);
        });
        if (bodyContents.size() > 0)
            bodyContents.remove(bodyContents.size() - 1);   //移除掉最後一個separator
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.MD).build();

        //標頭
        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("記帳小本本").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#F5D58C").build();

        //計算總金額
        BigDecimal total = listBookkeepingGroupByDate.keySet().stream().map(date -> {
            List<BookkeepingPO> listBookkeepingSameDate = listBookkeepingGroupByDate.get(date);
            return listBookkeepingSameDate.stream().map(item -> item.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);
        }).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);

        //footer total amount
        List<FlexComponent> footerContents = Arrays.asList(
                Text.builder().text("Total").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).build(),
                Text.builder().text("$" + total.setScale(2, BigDecimal.ROUND_HALF_UP)).size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.END).build()
        );
        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(footerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#F5D58C").build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(footer).build();

        //取得今天日期
        String data = "記 查 ";
        ZonedDateTime dateTime = ZonedDateTime.now(DateUtils.CST_ZONE_ID);
        String dayOfWeekDate = dateTime.minusDays(dateTime.getDayOfWeek().getValue()).format(DateUtils.yyyyMMdd);
        String dayOfMonthDate = dateTime.minusDays(dateTime.getDayOfMonth() - 1).format(DateUtils.yyyyMMdd);
        String minusMonthsDate = dateTime.minusMonths(3).format(DateUtils.yyyyMMdd);
        String dayOfYearDate = dateTime.minusDays(dateTime.getDayOfYear()).format(DateUtils.yyyyMMdd);
        QuickReply quickReply = QuickReply.builder().items(
                Arrays.asList(
                        QuickReplyItem.builder().action(PostbackAction.builder().label("本周").data(data + dayOfWeekDate + ParamterUtils.CONTACT + dateTime.format(DateUtils.yyyyMMdd)).build()).build(),
                        QuickReplyItem.builder().action(PostbackAction.builder().label("本月").data(data + dayOfMonthDate + ParamterUtils.CONTACT + dateTime.format(DateUtils.yyyyMMdd)).build()).build(),
                        QuickReplyItem.builder().action(PostbackAction.builder().label("三個月").data(data + minusMonthsDate + ParamterUtils.CONTACT + dateTime.format(DateUtils.yyyyMMdd)).build()).build(),
                        QuickReplyItem.builder().action(PostbackAction.builder().label("今年").data(data + dayOfYearDate + ParamterUtils.CONTACT + dateTime.format(DateUtils.yyyyMMdd)).build()).build()
                )).build();
        return FlexMessage.builder().altText("記帳小本本").contents(contents).quickReply(quickReply).build();
    }

}
