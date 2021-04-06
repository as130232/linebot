package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.CurrencyEnum;
import com.eachnow.linebot.common.db.po.BookkeepingPO;
import com.eachnow.linebot.common.db.repository.BookkeepingRepository;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.po.DescriptionCommandPO;
import com.eachnow.linebot.common.po.DescriptionPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
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
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
        commands.add(DescriptionCommandPO.builder().explain("記帳").command("記 {類型} {金額} {幣值(可省略)}").example("記 晚餐 100 台幣(可省略)").build());
        commands.add(DescriptionCommandPO.builder().explain("查帳").command("記 查 {開始時間(可省略)} {結束時間(可省略)}").example("記 查 20210101 20210103").build());
        return DescriptionPO.builder().title("記帳").description("支援多國幣值，記帳時可以在金額後輸入對應幣值，若省略預設為新台幣，查帳時的時間格式yyyyMMdd，未給時間則預設查詢當天日期。")
                .commands(commands).build();
    }

//    @PostConstruct
//    private void test() {
//        String text = "記 查 20210329";
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
        if (currencyEnum == null)
            currencyEnum = CurrencyEnum.TWD; //default 新台幣
        if (commandPO.getParams().size() < 2 || !isNumber(amount)) {
            return new TextMessage("請輸入正確格式:" + getFormat() + "，例:記 晚餐 180，注意需空格隔開！");
        }
        if (text.contains(CONFIRM)) {
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());    //移除緩存
            BookkeepingPO po = BookkeepingPO.builder().userId(commandPO.getUserId()).typeName(typeName).amount(new BigDecimal(amount)).currency(currencyEnum.toString())
                    .createTime(new Timestamp((DateUtils.getCurrentEpochMilli()))).build();
            bookkeepingRepository.save(po);
            log.info("記帳成功。BookkeepingPO:{}", po);
            return new TextMessage("記帳成功。");
        } else if (text.contains(CANCEL)) {
            MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());    //移除緩存
            return new TextMessage("記帳已取消。");
        }
        String data = commandPO.getCommand() + ParamterUtils.CONTACT + typeName + ParamterUtils.CONTACT +
                amount + ParamterUtils.CONTACT + currencyEnum.getName() + ParamterUtils.CONTACT;
        MessageHandler.setUserAndCacheCommand(commandPO.getUserId(), data); //新增緩存
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text("類型: " + typeName).size(FlexFontSize.LG).build(),
                Text.builder().text("金額: " + amount).size(FlexFontSize.LG).build(),
                Text.builder().text("幣值: " + currencyEnum.getName()).size(FlexFontSize.LG).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).margin(FlexMarginSize.SM).build();
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

    /**
     * 判斷該字串是否為整數或浮點數
     *
     * @param input 字串
     * @return 是返回true 否則返回false
     */
    public static boolean isNumber(String input) {
        if (input == null || "".equals(input)) {
            return false;
        }
        return Pattern.matches("[0-9]*(\\.?)[0-9]*", input);
    }

    public Message getBookkeeper(CommandPO commandPO) {
        String startDate = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        if (startDate == null)  //預設當天
            startDate = DateUtils.getCurrentDate(DateUtils.yyyyMMdd);
        long startDateTime = DateUtils.parseToStartOfDayMilli(startDate, DateUtils.yyyyMMdd);
        String endDate = ParamterUtils.getValueByIndex(commandPO.getParams(), 2);
        if (endDate == null)
            endDate = startDate;
        long endDateTime = DateUtils.parseToEndOfDayMilli(endDate, DateUtils.yyyyMMdd);
        List<BookkeepingPO> listBookkeeping = bookkeepingRepository.findByUserIdAndCreateTimeBetween(commandPO.getUserId(), new Timestamp(startDateTime), new Timestamp(endDateTime));
        //按照日期分類
        Map<String, List<BookkeepingPO>> listBookkeepingGroupByDate = listBookkeeping.stream().collect(Collectors.groupingBy(po -> DateUtils.format(po.getCreateTime(), DateUtils.yyyyMMddDash)));
        List<FlexComponent> bodyContents = new ArrayList<>();

        listBookkeepingGroupByDate.keySet().stream().forEach(date -> {
            List<BookkeepingPO> listBookkeepingSameDate = listBookkeepingGroupByDate.get(date);
            BigDecimal totalOneDay = listBookkeepingSameDate.stream().map(item -> item.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_HALF_UP);
            //一天所有資訊
            List<FlexComponent> oneDateContents = new ArrayList<>();
            Box oneDateAndTotal = Box.builder().layout(FlexLayout.HORIZONTAL).contents(Arrays.asList(
                    //日期
                    Text.builder().text(date.replace("-", "/")).size(FlexFontSize.Md).style(Text.TextStyle.ITALIC).weight(Text.TextWeight.BOLD).color("#555555").build(),
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
        String dayOfMonthDate = dateTime.minusDays(dateTime.getDayOfMonth()).format(DateUtils.yyyyMMdd);
        String minusMonthsDate = dateTime.minusMonths(3).format(DateUtils.yyyyMMdd);
        String dayOfYearDate = dateTime.minusDays(dateTime.getDayOfYear()).format(DateUtils.yyyyMMdd);
        QuickReply quickReply = QuickReply.builder().items(
                Arrays.asList(
                        QuickReplyItem.builder().action(PostbackAction.builder().label("本周").data(data + dayOfWeekDate + " " + dateTime.format(DateUtils.yyyyMMdd)).build()).build(),
                        QuickReplyItem.builder().action(PostbackAction.builder().label("本月").data(data + dayOfMonthDate + " " + dateTime.format(DateUtils.yyyyMMdd)).build()).build(),
                        QuickReplyItem.builder().action(PostbackAction.builder().label("三個月").data(data + minusMonthsDate + " " + dateTime.format(DateUtils.yyyyMMdd)).build()).build(),
                        QuickReplyItem.builder().action(PostbackAction.builder().label("今年").data(data + dayOfYearDate + " " + dateTime.format(DateUtils.yyyyMMdd)).build()).build()
                )).build();
        return FlexMessage.builder().altText("記帳小本本").contents(contents).quickReply(quickReply).build();
    }
}
