package com.eachnow.linebot.domain.service.handler.command.impl;

import com.eachnow.linebot.common.annotation.Command;
import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.db.repository.LineUserRepository;
import com.eachnow.linebot.common.po.CommandPO;
import com.eachnow.linebot.domain.service.handler.command.CommandHandler;
import com.eachnow.linebot.domain.service.line.LineNotifySender;
import com.eachnow.linebot.domain.service.line.MessageSender;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
@Command({"11"})
public class WhisperHandler implements CommandHandler {
    private Map<String, LineUserPO> userMap = new HashMap<>();
    private Integer TYPE_LINEBOT = 1;
    private Integer TYPE_NOTIFY = 2;

    private LineUserRepository lineUserRepository;
    private LineNotifySender lineNotifySender;
    private MessageSender messageSender;

    @Autowired
    public WhisperHandler(LineUserRepository lineUserRepository,
                          LineNotifySender lineNotifySender,
                          MessageSender messageSender) {
        this.lineUserRepository = lineUserRepository;
        this.lineNotifySender = lineNotifySender;
        this.messageSender = messageSender;
    }

    private Integer getType() {
        return TYPE_NOTIFY;
    }

    @Override
    public Message execute(CommandPO commandPO) {
        String text = commandPO.getText();
        String sendMessage = text.replace(commandPO.getCommand() + " ", "");
        String code = commandPO.getCommand();

        LineUserPO lineUserPO = userMap.get(code);
        if (Objects.isNull(lineUserPO)) {
            Optional<LineUserPO> optional = lineUserRepository.findById(code);
            if (!optional.isPresent()) {
                return new TextMessage("send failed.");
            }
            lineUserPO = optional.get();
            userMap.put(code, lineUserPO);
        }
        Message message = new TextMessage(sendMessage);
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        messageSender.push(lineUserPO.getId(), messages);
        return new TextMessage("send success.");
    }
}
