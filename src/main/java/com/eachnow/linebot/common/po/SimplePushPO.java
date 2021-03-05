package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimplePushPO {

    private String roomId;

    private String accountId;

    private SimpleContentPO content;

}
