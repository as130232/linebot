package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessagePO {
    //MessageConstants
    private String type;
    private String text;
    private String packageId;
    private String stickerId;
}
