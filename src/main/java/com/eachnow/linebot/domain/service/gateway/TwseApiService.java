package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.po.twse.IndexPO;
import com.eachnow.linebot.common.po.twse.PricePO;
import com.eachnow.linebot.common.po.twse.RatioAndDividendYieldPO;

import java.util.List;

/**
 * 台灣證交所API
 */
public interface TwseApiService {

    /**
     * 重新取得最新個股股價
     */
    public void initPriceMap();

    /**
     * 取得個股最新股價
     * @param code 股票代號
     */
    public PricePO getPrice(String code);

    /**
     * 取得當日所有個股股價
     */
    public List<PricePO> getStockPrice();

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

    /**
     * 取得個股本益比、股價淨值比及殖利率
     * @param date 日期 yyyyMMdd
     */
    public List<RatioAndDividendYieldPO> getRatioAndDividendYield(String date);
}
