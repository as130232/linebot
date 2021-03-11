package com.eachnow.linebot.common.po.google;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GoogleTranslationPO {
    private String q;
    private String target;
}
