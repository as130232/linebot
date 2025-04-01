package com.eachnow.linebot.common.constant;

import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum WeatherElementEnum {
    WX("Wx", "氣溫"),
    MAX_T("MaxT", "最高溫度"),
    MIN_T("MinT", "最低溫度"),
    CI("CI", "舒適度"),
    POP("PoP", "降雨機率"),
    WIND_DIR("WindDir", "風向"),
    WIND_SPEED("WindSpeed", "風速"),
    WAVE_HEIGHT("WaveHeight", "浪高"),
    WAVE_TYPE("WaveType", "浪況"),
    ;

    private String element;
    private String name;

    WeatherElementEnum(String element, String name) {
        this.element = element;
        this.name = name;
    }

    public static String getElement(String name) {
        if (Strings.isEmpty(name))
            return null;
        Optional<WeatherElementEnum> optional = Arrays.stream(WeatherElementEnum.values())
                .filter(langEnum -> langEnum.getName().contains(name)).findFirst();
        if (optional.isPresent()) {
            return optional.get().getElement();
        }
        return null;
    }

    public static String getName(String element) {
        if (Strings.isEmpty(element))
            return null;
        Optional<WeatherElementEnum> optional = Arrays.stream(WeatherElementEnum.values())
                .filter(langEnum -> langEnum.getElement().contains(element)).findFirst();
        if (optional.isPresent()) {
            return optional.get().getName();
        }
        return WeatherElementEnum.WX.getName(); //default
    }

    public static String getRainIcon(int unit) {
        if (unit > 0 && unit <= 30) {
            return "⛅️";
        } else if (unit > 30 && unit <= 50) {
            return "🌦️";
        } else if (unit > 50 && unit <= 70) {
            return "🌧";
        } else if (unit > 70 && unit <= 100) {
            return "⛈️️";
        } else {
            return "☀️";
        }
    }
}
