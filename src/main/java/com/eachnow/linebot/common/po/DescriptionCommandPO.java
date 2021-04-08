package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DescriptionCommandPO {
    private String explain;
    private String command;
    private String example;
    private String postback;
}
