package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.annotation.Description;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Slf4j
@Description("翻譯(選擇對應語系按鈕)")
@Command(value = {"translate", "翻譯", "@translate"})
public class TranslationButtonHandler implements CommandHandler {
    private GoogleApiService googleApiService;
    private LanguageEnum lang;

    @Autowired
    public TranslationButtonHandler(GoogleApiService googleApiService) {
        this.googleApiService = googleApiService;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String translateKey = "@translate ";
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
        return new TextMessage("Choose translation language.\nPlease input @close when you end. 結束時請輸入@close關閉翻譯模式.", quickReply);
    }

}
