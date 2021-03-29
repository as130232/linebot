package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.annotation.Description;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.MessageHandler;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;

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
            return new TextMessage("Choose translation language.\nPlease input @close when you end. 結束時請輸入@close關閉翻譯模式.", quickReply);
        }
        LanguageEnum lang = commandPO.getParams().size() > 0 ? LanguageEnum.parse(commandPO.getParams().get(0)) : null;
        String word = commandPO.getParams().size() > 1 ? commandPO.getParams().get(1) : null;
        //設置緩存
        if (lang != null)
            MessageHandler.setUserAndCacheCommand(commandPO.getUserId(), commandPO.getCommand() + ParamterUtils.CONTACT + lang.getCode() + ParamterUtils.CONTACT);
        if (lang != null && word == null) {
            return new TextMessage("[Translate mode已開啟翻譯模式]\n翻譯語言:" + lang.getCode() + ", 請輸入欲翻譯文字。");
        }
        String result = googleApiService.translate(word, lang.getLang());
        return new TextMessage(result);
    }

}
