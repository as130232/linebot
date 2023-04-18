package com.eachnow.linebot.domain.service.handler.command;

import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.common.util.NumberUtils;
import com.eachnow.linebot.domain.service.handler.DefaultHandler;
import com.eachnow.linebot.domain.service.handler.command.impl.CloseCommandHandler;
import com.eachnow.linebot.domain.service.handler.command.impl.WhisperHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 此處不可加@Component，否則handlerConfig初始handlerMap時會失效
 */
@Slf4j
public class CommandHandlerFactory {
    private Map<String, Class<? extends CommandHandler>> handlerMap;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DefaultHandler defaultHandler;
    @Autowired
    private CloseCommandHandler closeCommandHandler;
    @Autowired
    private WhisperHandler whisperHandler;

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
        //密語
        if (command.length() == 2 && NumberUtils.isNumber(command))
            return whisperHandler;

        if (Strings.isEmpty(command))
            return commandHandler;

        Class<? extends CommandHandler> commandHandlerClass = handlerMap.get(command.toLowerCase());
        if (handlerMap == null || commandHandlerClass == null) {
            log.warn("Can not get the Class of CommandHandler, command:{}", command);
            return commandHandler;
        }
        return applicationContext.getBean(commandHandlerClass);
    }
}
