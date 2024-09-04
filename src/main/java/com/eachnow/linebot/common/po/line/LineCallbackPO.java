package com.eachnow.linebot.common.po.line;

import com.linecorp.bot.model.event.MessageEvent;
import lombok.Data;

import java.util.List;

@Data
public class LineCallbackPO {
    private String destination;
    private List<MessageEvent> events;
}
