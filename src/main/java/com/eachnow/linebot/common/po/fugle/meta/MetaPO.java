package com.eachnow.linebot.common.po.fugle.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetaPO {
    private Boolean isIndex;        //是否為指數
    private String nameZhTw;        //股票中文簡稱
    private String industryZhTw;    //產業別
    private Integer priceReference; //今日參考價
    private Integer priceHighLimit; //漲停價
    private Integer priceLowLimit;  //跌停價
    private Boolean canDayBuySell;  //是否可先買後賣現股當沖
    private Boolean canDaySellBuy;  //是否可先賣後買現股當沖
    private Boolean canShortMargin; //是否豁免平盤下融券賣出
    private Boolean canShortLend;   //是否豁免平盤下借券賣出
    private Integer volumePerUnit;  //交易單位：股/張
    private String currency;        //交易幣別代號
    private Boolean isTerminated;   //今日是否已終止上市
    private Boolean isSuspended;    //今日是否暫停買賣
    private Boolean isWarrant;      //是否為權證
    private String typeZhTw;        //股票類別
    private String abnormal;        //警示或處置股標示 (正常、注意、處置、注意及處置、再次處置、注意及再次處置、彈性處置、注意及彈性處置)
    private Boolean isUnusuallyRecommended;
}
