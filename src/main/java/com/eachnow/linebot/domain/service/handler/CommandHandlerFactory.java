package com.eachnow.linebot.domain.service.handler;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.util.ParamterUtils;
import com.eachnow.linebot.domain.service.handler.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 此處不可加@Component，否則handlerConfig初始handlerMap時會失效
 */
@Slf4j
public class CommandHandlerFactory {
    private Map<String, Class<? extends CommandHandler>> handlerMap;
    private ApplicationContext applicationContext;

    @Autowired
    private ActressHandler actressHandler;
    @Autowired
    private BeautyHandler beautyHandler;
    @Autowired
    private InstagramHandler instagramHandler;
    @Autowired
    private TranslationHandler translationHandler;
    @Autowired
    private WeatherHandler weatherHandler;
    @Autowired
    private EatWhatHandler eatWhatHandler;
    @Autowired
    private RestaurantHandler restaurantHandler;
    @Autowired
    private HelloHandler helloHandler;

    @Autowired
    private DefaultHandler defaultHandler;

    @Autowired
    private CloseResidentCommandHandler closeResidentCommandHandler;
    //常駐指令，直到下達關閉才結束
    private CommandHandler residentCommandHandler;

    public CommandHandlerFactory(Map<String, Class<? extends CommandHandler>> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public CommandHandler getCommandHandler(String text) {
        String command = ParamterUtils.parseCommand(text);
        log.info("command:{}", text);
        CommandHandler commandHandler = defaultHandler;
        //若已下達常駐指令，則直到關閉為止
        if (residentCommandHandler != null && !text.contains("@close"))
            return residentCommandHandler;
        if (command == null || "".equals(command))
            return commandHandler;

        Class<? extends CommandHandler> commandHandlerClass = handlerMap.get(command.toLowerCase());
        if (handlerMap == null) {
            log.warn("Can not get the Class of CommandHandler, command:{}", command);
            return commandHandler;
        }
        try {
            if (BeautyHandler.class.equals(commandHandlerClass)) {
                commandHandler = beautyHandler;
            } else if (ActressHandler.class.equals(commandHandlerClass)) {
                commandHandler = actressHandler;
            } else if (InstagramHandler.class.equals(commandHandlerClass)) {
                commandHandler = instagramHandler;
            } else if (WeatherHandler.class.equals(commandHandlerClass)) {
                commandHandler = weatherHandler;
            } else if (EatWhatHandler.class.equals(commandHandlerClass)) {
                commandHandler = eatWhatHandler;
            } else if (RestaurantHandler.class.equals(commandHandlerClass)) {
                commandHandler = restaurantHandler;
            } else if (HelloHandler.class.equals(commandHandlerClass)) {
                commandHandler = helloHandler;
            } else if (TranslationHandler.class.equals(commandHandlerClass)) {
                commandHandler = translationHandler;
                translationHandler.setCurrentLang(text);    //設定翻譯語言
            } else if (CloseResidentCommandHandler.class.equals(commandHandlerClass)) {
                residentCommandHandler = null;   //關閉常駐指令
                commandHandler = closeResidentCommandHandler;
            }
            //檢查該指令是否為常駐指令
            Command commandClass = commandHandlerClass.getAnnotation(Command.class);
            if (commandClass.resident())
                residentCommandHandler = commandHandler;
            return commandHandler;
//            result = applicationContext.getBean(commandHandlerClass);
//            result = commandHandlerClass.newInstance();
        } catch (Exception e) {
            log.warn("CommandHandler newInstance failed! command:{}", command);
        }
        return commandHandler;
    }
}
