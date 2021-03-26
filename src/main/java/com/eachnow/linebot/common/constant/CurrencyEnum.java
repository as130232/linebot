package com.eachnow.linebot.common.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public enum CurrencyEnum {
    USD("USDUSD", "美金"),
    TWD("USDTWD", "台幣"),
    CNY("USDCNY", "人民幣"),
    HKD("USDHKD", "港元"),
    MOP("USDMOP", "澳門幣"),
    JPY("USDJPY", "日幣"),
    KRW("USDKRW", "韓元"),
    SGD("USDSGD", "新加坡元"),
    THB("USDTHB", "泰銖"),
    VND("USDVND", "越南盾"),
    INR("USDINR", "印度盧比"),
    GBP("USDGBP", "英鎊"),
    EUR("USDEUR", "歐元"),
    CHF("USDCHF", "瑞士法郎"),
    BTC("USDBTC", "比特幣"),
    LTC("USDLTC", "萊特幣"),
    ;

    private String key;
    private String name;

    CurrencyEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public static CurrencyEnum parse(String name) {
        Optional<CurrencyEnum> optional = Arrays.stream(CurrencyEnum.values()).filter(langEnum -> langEnum.getName().contains(name)).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null; //default
    }

    public static List<CurrencyEnum> commonCurrency() {
        List<CurrencyEnum> result = Arrays.asList(TWD, CNY, JPY, KRW, GBP, EUR, BTC);
        return result;
    }

    public static List<CurrencyEnum> listCurrencyForQuickReply() {
        List<CurrencyEnum> result = Arrays.asList(USD, TWD, CNY, HKD, MOP, JPY, KRW, SGD, THB, GBP, EUR, CHF, BTC);
        return result;
    }


}
