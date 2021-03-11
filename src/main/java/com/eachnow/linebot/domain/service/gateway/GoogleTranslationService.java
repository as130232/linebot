package com.eachnow.linebot.domain.service.gateway;

import java.util.Locale;

public interface GoogleTranslationService {
    String translate(String text, String code);
}
