package com.eachnow.linebot.common.po.currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyPO {
    private ExratePO USDUSD;    //美金
    private ExratePO USDTWD;    //新台幣
    private ExratePO USDHKD;    //港元
    private ExratePO USDMOP;    //澳門幣
    private ExratePO USDCNY;    //人民幣
    private ExratePO USDJPY;    //日幣
    private ExratePO USDKRW;    //韓元
    private ExratePO USDSGD;    //新加坡元
    private ExratePO USDTHB;    //泰銖
    private ExratePO USDINR;    //印度盧比
    private ExratePO USDVND;    //越南盾
    private ExratePO USDGBP;    //英鎊
    private ExratePO USDEUR;    //歐元
    private ExratePO USDCHF;    //瑞士法郎
    private ExratePO USDBTC;    //比特幣
    private ExratePO USDLTC;    //萊特幣
}
