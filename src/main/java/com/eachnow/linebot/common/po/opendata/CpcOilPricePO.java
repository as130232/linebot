package com.eachnow.linebot.common.po.opendata;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中油及時油價 單位：元/公升
 * unleadedGasoline
 */
@Data
@NoArgsConstructor
public class CpcOilPricePO {
    @JsonAlias("PriceUpdate")
    private String priceUpdate;     //8月30日
    @JsonAlias("UpOrDown_Html")
    private String upOrDownHtml;   //<div class=\"ups_and_downs ups\"><b class=\"name\">本週汽油價格</b><b class=\"sys\">調漲</b><b class=\"rate\"><i>0.4</i></b></div>
    @JsonAlias("sPrice1")
    private String price92;         //92無鉛 or 指標原油 N/A 美元/桶(type=TargetOilPrice)
    @JsonAlias("sPrice2")
    private String price95;         //95無鉛 or 匯率 27.813 新台幣兌美元(type=TargetOilPrice)
    @JsonAlias("sPrice3")
    private String price98;         //98無鉛
    @JsonAlias("sPrice4")
    private String priceAlcohol;    //酒精汽油
    @JsonAlias("sPrice5")
    private String priceDiesel;     //超級柴油
    @JsonAlias("sPrice6")
    private String priceLpg;        //液化石油氣
    private String LPGdate;         //液化石油氣價格日期
}
