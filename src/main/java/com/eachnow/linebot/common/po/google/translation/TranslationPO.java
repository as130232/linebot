package com.eachnow.linebot.common.po.google.translation;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TranslationPO {
    private String translatedText;
    private String detectedSourceLanguage;
}
