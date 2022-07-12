package com.eachnow.linebot.common.util;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

public class CurrencyUtils {

    /**
     * 取得該匯率JSON物件中該key(幣值)的匯率
     *
     * @param json 匯率JSON物件
     * @param key  幣值
     * @return 匯率
     */
    public static BigDecimal getExrate(JSONObject json, String key) {
        Object obj = json.get(key);
        if (!Objects.isNull(obj)) {
            JSONObject valueObj = new JSONObject(obj.toString());
            return new BigDecimal(valueObj.get("Exrate").toString());
        }
        return null;
    }

    /**
     * 取得該匯率JSON物件中該key(幣值)的最後更新時間
     *
     * @param json 匯率JSON物件
     * @param key  幣值
     * @return 匯率
     */
    public static String getUtc(JSONObject json, String key) {
        Object obj = json.get(key);
        if (!Objects.isNull(obj)) {
            JSONObject valueObj = new JSONObject(obj.toString());
            return valueObj.get("UTC").toString();
        }
        return null;
    }
}
