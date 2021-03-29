package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
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
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexOffsetSize;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Command({"記", "記帳"})
public class BookkeepingHandler implements CommandHandler {

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        if (commandPO.getParams().size() < 2)
            return new TextMessage("請輸入正確格式:" + getFormat() + "，例:記 晚餐 180，注意需空格隔開！");
        String type = commandPO.getParams().get(0);
        String amount = commandPO.getParams().get(1);
        if (text.contains("@confirm")) {

        }
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text("請問輸入正確嗎?").weight(Text.TextWeight.BOLD).size(FlexFontSize.LG).align(FlexAlign.CENTER).build(),  //標頭
                Text.builder().text("金額: " + amount).size(FlexFontSize.LG).offsetTop(FlexOffsetSize.SM).build(),
                Text.builder().text("類型: " + type).size(FlexFontSize.LG).offsetTop(FlexOffsetSize.MD).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build();
        String confirm = text + "@confirm";
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("確定").data(confirm).build()).build(),
                Button.builder().style(Button.ButtonStyle.SECONDARY).height(Button.ButtonHeight.SMALL).action(PostbackAction.builder().label("取消").build()).build()
        );
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(footerContents).build();
        FlexContainer contents = Bubble.builder().header(null).hero(null).body(body).footer(footer).build();
        return new FlexMessage("記帳確認", contents);
    }

    public String getFormat() {
        return "記 {類型} {金額}";
    }
}
