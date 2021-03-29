//package com.eachnow.linebot.domain.service.handler.command.impl;
//
//import com.eachnow.linebot.common.annotation.Command;
//import com.eachnow.linebot.common.constant.LanguageEnum;
//import com.eachnow.linebot.common.po.CommandPO;
//import com.eachnow.linebot.common.util.ParamterUtils;
//import com.eachnow.linebot.domain.service.gateway.GoogleApiService;
//import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
//import com.linecorp.bot.model.message.Message;
//import com.linecorp.bot.model.message.TextMessage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Slf4j
//@Command(value = {"@translate", "@翻譯"}, resident = true)
//public class TranslationHandler implements CommandHandler {
//    private GoogleApiService googleApiService;
//    private LanguageEnum lang;
//
//    @Autowired
//    public TranslationHandler(GoogleApiService googleApiService) {
//        this.googleApiService = googleApiService;
//    }
//
//    @Override
//    public Message execute(CommandPO commandPO) {
//        String parameters = commandPO.getText();
//        if (parameters.contains("@translate") || parameters.contains("@翻譯"))
//            return new TextMessage("[Translate mode已開啟翻譯模式]");
//        //當已開啟翻譯模式時，commandHandlerFactory則會一直回傳該TranslationHandler，即使text中不含"@translate"字眼，值到用戶輸入@close後關閉該常駐程式
//        String result = googleApiService.translate(parameters, lang.getLang());
//        return new TextMessage(result);
//    }
//
//    public void setCurrentLang(String text) {
//        this.lang = LanguageEnum.parse(ParamterUtils.getIndexOneParameter(text));
//    }
//
//}
