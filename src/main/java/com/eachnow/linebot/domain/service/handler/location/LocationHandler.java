package com.eachnow.linebot.domain.service.handler.location;

import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.message.Message;

public interface LocationHandler {
    public Message execute(LocationMessageContent content);
}
