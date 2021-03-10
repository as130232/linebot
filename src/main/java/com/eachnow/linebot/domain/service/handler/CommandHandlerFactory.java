package com.eachnow.linebot.domain.service.handler;

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
    private BeautyHandler beautyHandler;
    @Autowired
    private InstagramHandler instagramHandler;
    @Autowired
    private WeatherHandler weatherHandler;
    @Autowired
    private EatWhatHandler eatWhatHandler;
    @Autowired
    private HelloHandler helloHandler;

    @Autowired
    private DefaultHandler defaultHandler;

    public CommandHandlerFactory(Map<String, Class<? extends CommandHandler>> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public CommandHandler getCommandHandler(String command) {
        log.info("command:{}", command);
        CommandHandler result = defaultHandler;
        if (command == null || "".equals(command))
            return result;
        Class<? extends CommandHandler> commandHandlerClass = handlerMap.get(command.toLowerCase());
        if (handlerMap == null) {
            log.warn("Can not get the Class of CommandHandler, command:{}", command);
            return result;
        }
        try {
            if (BeautyHandler.class.equals(commandHandlerClass)) {
                return beautyHandler;
            } else if (InstagramHandler.class.equals(commandHandlerClass)) {
                return instagramHandler;
            } else if (WeatherHandler.class.equals(commandHandlerClass)) {
                return weatherHandler;
            } else if (EatWhatHandler.class.equals(commandHandlerClass)) {
                return eatWhatHandler;
            } else if (HelloHandler.class.equals(commandHandlerClass)) {
                return helloHandler;
            }
//            result = applicationContext.getBean(commandHandlerClass);
//            result = commandHandlerClass.newInstance();
        } catch (Exception e) {
            log.warn("CommandHandler newInstance failed! command:{}", command);
        }
        return result;
    }
}
