package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.CurrencyEnum;
import com.eachnow.linebot.common.db.po.BookkeepingPO;
import com.eachnow.linebot.common.db.repository.BookkeepingRepository;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Command({"記", "記帳"})
public class BookkeepingHandler implements CommandHandler {
    private BookkeepingRepository bookkeepingRepository;
    private final String CONFIRM = "@confirm";
    private final String CANCEL = "@cancel";

    @Autowired
    private BookkeepingHandler(BookkeepingRepository bookkeepingRepository) {
        this.bookkeepingRepository = bookkeepingRepository;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        String typeName = ParamterUtils.getValueByIndex(commandPO.getParams(), 0);
        String amount = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        CurrencyEnum currencyEnum = CurrencyEnum.parse(ParamterUtils.getValueByIndex(commandPO.getParams(), 2));
        if (currencyEnum == null)
            currencyEnum = CurrencyEnum.TWD; //default 新台幣
        if (commandPO.getParams().size() < 2 || !isNumber(amount)) {
            return new TextMessage("請輸入正確格式:" + getFormat() + "，例:記 晚餐 180，注意需空格隔開！");
        }

        if (text.contains(CONFIRM)) {
            BookkeepingPO po = BookkeepingPO.builder().userId(commandPO.getUserId()).typeName(typeName).amount(new BigDecimal(amount)).currency(currencyEnum.toString()).build();
            bookkeepingRepository.save(po);
            log.info("記帳成功。BookkeepingPO:{}", po);
            return new TextMessage("記帳成功。");
        } else if (text.contains(CANCEL)) {
            return new TextMessage("記帳已取消。");
        }
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text("金額: " + amount).size(FlexFontSize.LG).build(),
                Text.builder().text("幣值: " + currencyEnum.getName()).size(FlexFontSize.LG).build(),
                Text.builder().text("類型: " + typeName).size(FlexFontSize.LG).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).margin(FlexMarginSize.SM).build();
        String data = commandPO.getCommand() + ParamterUtils.CONTACT + typeName + ParamterUtils.CONTACT +
                amount + ParamterUtils.CONTACT + currencyEnum.getName() + ParamterUtils.CONTACT;
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("確定").data(data + CONFIRM).build()).build(),
                Button.builder().style(Button.ButtonStyle.SECONDARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("取消").data(data + CANCEL).build()).build()
        );
        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("請問輸入正確嗎?").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#29bae6").build();
        Box footer = Box.builder().layout(FlexLayout.HORIZONTAL).contents(footerContents).build();
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

    @PostConstruct
    private void test() {
        String text = "記 晚餐 180";
        CommandPO commandPO = CommandPO.builder().userId("userId").text(text)
                .command(ParamterUtils.parseCommand(text)).params(ParamterUtils.listParameter(text)).build();
        this.execute(commandPO);
    }
}
