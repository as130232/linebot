package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexPaddingSize;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Command(value = {"translate", "翻譯"})
public class TranslationHandler implements CommandHandler {
    private GoogleApiService googleApiService;

    @Autowired
    public TranslationHandler(GoogleApiService googleApiService) {
        this.googleApiService = googleApiService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        //若只有給指令沒有給參數，顯示選擇語言按鈕
        if (text.equals(commandPO.getCommand())) {
            String translateKey = this.getClass().getAnnotation(Command.class).value()[0] + ParamterUtils.CONTACT;
            QuickReply quickReply = QuickReply.builder().items(
                    Arrays.asList(
                            QuickReplyItem.builder().action(PostbackAction.builder().label("English(英)").data(translateKey + "英").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("T-Chinese(繁中)").data(translateKey + "中文").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("S-Chinese(簡中)").data(translateKey + "簡體").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("Japan(日)").data(translateKey + "日").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("Korean(韓)").data(translateKey + "韓").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("Thai(泰)").data(translateKey + "泰").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("Indonesian(印尼)").data(translateKey + "印尼").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("Vietnamese(越)").data(translateKey + "越").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("French(法)").data(translateKey + "法").build()).build(),
                            QuickReplyItem.builder().action(PostbackAction.builder().label("Russian(俄)").data(translateKey + "俄").build()).build()
                    )).build();
            List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("翻譯").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
            Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#A17DF5").build();
            List<FlexComponent> bodyContents = Arrays.asList(
                    Text.builder().text("請選擇翻譯語言(按鈕)").weight(Text.TextWeight.BOLD).build(),
                    Text.builder().text("結束時請輸入 @close 關閉翻譯模式").build()
            );
            Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.XL).build();
            FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(null).build();
            return FlexMessage.builder().altText("Translate翻譯").contents(contents).quickReply(quickReply).build();
        }
        LanguageEnum lang = LanguageEnum.parse(ParamterUtils.getValueByIndex(commandPO.getParams(), 0));
        String word = ParamterUtils.getValueByIndex(commandPO.getParams(), 1);
        //設置緩存
        if (lang != null)
            MessageHandler.setUserAndCacheCommand(commandPO.getUserId(), commandPO.getCommand() + ParamterUtils.CONTACT + lang.getCode() + ParamterUtils.CONTACT);
        if (lang != null && word == null) {
            List<FlexComponent> headerContents = Arrays.asList(Text.builder().text("已開啟翻譯模式").size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
            Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#A17DF5").build();
            List<FlexComponent> bodyContents = Arrays.asList(
                    Text.builder().text("翻譯語言: " + lang.getCode()).build(),
                    Text.builder().text("請輸入欲翻譯文字。").build()
            );
            Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).paddingAll(FlexPaddingSize.XL).build();
            FlexContainer contents = Bubble.builder().header(header).hero(null).body(body).footer(null).build();
            return FlexMessage.builder().altText("已開啟翻譯模式").contents(contents).build();
        }
        String result = googleApiService.translate(word, lang.getLang());
        return new TextMessage(result);
    }

}
