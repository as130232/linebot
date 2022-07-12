package com.eachnow.linebot.common.po.google.translation;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InputTranslationPO {
    private String q;
    private String target;
}
