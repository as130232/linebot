package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 指數PO
 */
@Builder
@Data
public class IndexPO {
    private String name;    //指數名稱
    private String tradeVolume; //成交股數
    private String tradeValue;  //成交金額
    private String transaction; //成交筆數(交易量)
    private Float change;      //漲跌指數

    private String taiex;   //台灣大盤加權指數
    private String date;   //日期

    public String getName() {
        if (this.name != null && this.name.contains("類指數"))
            return this.name.replace("類指數", "");
        return this.name;
    }

    public String getTradeValue() {
        //移除逗點
        String amount = this.tradeValue.replace(",", "");
        //單位從元轉為萬
        BigDecimal result = (new BigDecimal(amount)).divide(new BigDecimal(10000)).setScale(0, BigDecimal.ROUND_HALF_UP);
        return result.toString();
    }

}
