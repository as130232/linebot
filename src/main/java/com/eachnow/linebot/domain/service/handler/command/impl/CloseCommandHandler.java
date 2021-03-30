package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexPaddingSize;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Command({"@close", "@關"})
public class CloseCommandHandler implements CommandHandler {
    @Override
    public Message execute(CommandPO commandPO) {
        MessageHandler.removeUserAndCacheCommand(commandPO.getUserId());
        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("已關閉常駐指令模式").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#FF6B6E").build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(null).footer(null).build();
        return FlexMessage.builder().altText("已關閉常駐指令模式").contents(contents).build();

//        return new TextMessage("[Shut down mode.已關閉常駐指令模式]");
    }
}
