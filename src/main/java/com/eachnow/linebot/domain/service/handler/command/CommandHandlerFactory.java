package com.eachnow.linebot.domain.service.handler.command;

import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.handler.DefaultHandler;
import com.eachnow.linebot.domain.service.handler.command.impl.*;
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
    private BarHandler barHandler;
    @Autowired
    private PlaceHandler placeHandler;
    @Autowired
    private HelloHandler helloHandler;
    @Autowired
    private BookkeepingHandler bookkeepingHandler;
    @Autowired
    private CurrencyHandler currencyHandler;

    @Autowired
    private FuntionHandler funtionHandler;
    @Autowired
    private DefaultHandler defaultHandler;

    @Autowired
    private CloseCommandHandler closeCommandHandler;

    public CommandHandlerFactory(Map<String, Class<? extends CommandHandler>> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public CommandHandler getCommandHandler(CommandPO commandPO) {
        String text = commandPO.getText();
        String command = commandPO.getCommand();
        log.info("text:{}", text);
        CommandHandler commandHandler = defaultHandler;
        //若已下達常駐指令，則直到下達關閉為止
        if (text.contains("@close") || text.contains("@關"))
            return closeCommandHandler;
        if (command == null || "".equals(command))
            return commandHandler;

        Class<? extends CommandHandler> commandHandlerClass = handlerMap.get(command.toLowerCase());
        if (handlerMap == null) {
            log.warn("Can not get the Class of CommandHandler, command:{}", command);
            return commandHandler;
        }
        if (BeautyHandler.class.equals(commandHandlerClass)) {
            commandHandler = beautyHandler;
        } else if (ActressHandler.class.equals(commandHandlerClass)) {
            commandHandler = actressHandler;
        } else if (InstagramHandler.class.equals(commandHandlerClass)) {
            commandHandler = instagramHandler;
        } else if (BookkeepingHandler.class.equals(commandHandlerClass)) {
            commandHandler = bookkeepingHandler;
        } else if (CurrencyHandler.class.equals(commandHandlerClass)) {
            commandHandler = currencyHandler;
        } else if (WeatherHandler.class.equals(commandHandlerClass)) {
            commandHandler = weatherHandler;
        } else if (EatWhatHandler.class.equals(commandHandlerClass)) {
            commandHandler = eatWhatHandler;
        } else if (RestaurantHandler.class.equals(commandHandlerClass)) {
            commandHandler = restaurantHandler;
        } else if (BarHandler.class.equals(commandHandlerClass)) {
            commandHandler = barHandler;
        } else if (PlaceHandler.class.equals(commandHandlerClass)) {
            commandHandler = placeHandler;
        } else if (HelloHandler.class.equals(commandHandlerClass)) {
            commandHandler = helloHandler;
        } else if (TranslationHandler.class.equals(commandHandlerClass)) {
            commandHandler = translationHandler;
        } else if (FuntionHandler.class.equals(commandHandlerClass)) {
            commandHandler = funtionHandler;
        }
        return commandHandler;
//        try {
//            return applicationContext.getBean(commandHandlerClass);
//            result = commandHandlerClass.newInstance();
//        } catch (Exception e) {
//            log.warn("CommandHandler newInstance failed! command:{}", command);
//        }
    }
}
