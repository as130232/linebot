package com.eachnow.linebot.common.constant;

import lombok.Getter;

@Getter
public enum InstagramParamEnum {
    ACCOUNT("a"),
    RECENT("r"),
    COLLECTION("c");

    private String value;

    InstagramParamEnum(String value) {
        this.value = value;
    }

    public static InstagramParamEnum getParameter(String value) {
        switch (value) {
            case "a":
                return ACCOUNT;
            case "r":
                return RECENT;
            case "c":
                return COLLECTION;
            default:
                throw new UnsupportedOperationException();
        }
    }

//    public String getValue() {
//        return "-" + value;
//    }
}
