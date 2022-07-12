package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.currency.CurrencyPO;
import org.json.JSONObject;

public interface CurrencyService {
    /**
     * 取得即時匯率
     */
    public JSONObject getCurrency();
}
