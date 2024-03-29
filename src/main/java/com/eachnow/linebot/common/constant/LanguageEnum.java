package com.eachnow.linebot.common.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum LanguageEnum {
    TW("中文", "zh-tw"),
    CN("簡體", "zh-cn"),
    EN("英", "en"),
    FR("法", "fr"),
    JA("日", "ja"),
    KO("韓", "ko"),
    TH("泰", "th"),
    ID("印尼", "id"),
    VI("越", "vi"),
    RU("俄", "ru"),
    ;

    private String code;
    private String lang;

    LanguageEnum(String code, String lang) {
        this.code = code;
        this.lang = lang;
    }

    public static LanguageEnum parse(String code) {
        Optional<LanguageEnum> optional = Arrays.stream(LanguageEnum.values()).filter(langEnum -> langEnum.getCode().contains(code)).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
