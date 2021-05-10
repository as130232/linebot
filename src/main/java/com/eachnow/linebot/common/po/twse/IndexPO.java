package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

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
        if (this.name != null && this.name.contains("指數"))
            return this.name.replace("指數", "");
        return this.name;
    }

}
