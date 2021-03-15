package com.eachnow.linebot.domain.service.handler.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.constant.LanguageEnum;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.gateway.GoogleTranslationService;
import com.eachnow.linebot.domain.service.handler.CommandHandler;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Command(value = {"@translate"}, resident = true)
public class TranslationHandler implements CommandHandler {
    private GoogleTranslationService googleTranslationService;
    private LanguageEnum lang;

    @Autowired
    public TranslationHandler(GoogleTranslationService googleTranslationService) {
        this.googleTranslationService = googleTranslationService;
    }

    @Override
    public Message execute(String parameters) {
        if (parameters.contains("[translate]")) {
            return new TextMessage("[已開啟翻譯模式]");
        }
        log.info("欲翻譯語言:{}, 翻譯字串:{}", lang, parameters);
        String result = googleTranslationService.translate(parameters, lang.getLang());
        return new TextMessage(result);
    }

    public void setCurrentLang(String text) {
        this.lang = LanguageEnum.parse(ParamterUtils.getParameter(text));
    }

}
