package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PushMessagePO {
//    private String roomId;
//    private String accountId;
//    private SimpleContentPO content;
    private String to;
    private List<MessagePO> messages;
}
