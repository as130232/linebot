package com.eachnow.linebot.common.constant;

import lombok.Getter;

@Getter
public enum InstagramParameter {
    ACCOUNT("a"),
    RECENT("r"),
    COLLECTION("c");

    private String value;

    InstagramParameter(String value) {
        this.value = value;
    }

    public static InstagramParameter getParameter(String value) {
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
