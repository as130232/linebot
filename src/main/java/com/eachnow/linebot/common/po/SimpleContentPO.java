package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleContentPO {
    private String type;
    private String text;
}
