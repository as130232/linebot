package com.eachnow.linebot.config;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.handler.command.CommandHandlerFactory;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class HandlerConfig {
    @Bean
    public CommandHandlerFactory getHandlerCommandFactory() {
        Reflections reflections = new Reflections("com.eachnow.linebot.domain.service.handler.command");
        Map<String, Class<? extends CommandHandler>> handlerMap = new HashMap<>();

        Set<Class<? extends CommandHandler>> listCommandHandler = reflections.getSubTypesOf(CommandHandler.class);
        for (Class<? extends CommandHandler> commandHandlerClass : listCommandHandler) {
            Command commandArr = commandHandlerClass.getAnnotation(Command.class);
            if (commandArr == null)
                continue;
            String[] commands = commandArr.value();
            for (String command : commands) {
                handlerMap.put(command, commandHandlerClass);
//                handlerMap.put(command, (Constructor<? extends CommandHandler>) commandHandlerClass.getDeclaredConstructors()[0]);
            }
        }
        return new CommandHandlerFactory(handlerMap);
    }

}
