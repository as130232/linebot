package com.eachnow.linebot.common.po.google;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GoogletTranslationsPO {
    private String translatedText;
    private String detectedSourceLanguage;
}
