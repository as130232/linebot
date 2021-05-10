package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.twse.IndexPO;

import java.util.List;

/**
 * 台灣證交所API
 */
public interface TwseApiService {

    /**
     * 取得台股大盤指數成交量值
     * @param date 日期 yyyyMMdd
     */
    public IndexPO getDailyTradingOfTaiwanIndex(String date);

    /**
     * 取得台股各類指數成交量值
     * @param date 日期 yyyyMMdd
     */
    public List<IndexPO> getDailyTradeSummaryOfAllIndex(String date);
}
