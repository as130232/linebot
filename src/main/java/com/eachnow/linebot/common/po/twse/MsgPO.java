package com.eachnow.linebot.common.po.twse;

import com.eachnow.linebot.common.util.NumberUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MsgPO {

    private String c;   //股票編號
    private String ch;  //股票編號.tw
    private String ex;  //證交所代號:tse
    private String n;   //股票中文名稱:台積電
    private String nf;  //公司詳細名稱:台灣積體電路製造股份有限公司

    private String o;   //開盤價(09:00時的價格)
    private String h;   //當日最高價
    private String l;   //當日最低價
    private String z;   //現價or收盤價，有時會給"-"
    private String y;   //平盤價
    private String u;   //漲停價
    private String w;   //跌停價

    private String v;   //交易量(13:30以前不包含14:30)
    private String s;   //單量-盤中結束時交易量(13:30)
    private String tv;  //單量-盤中結束時交易量(13:30)
    private String fv;  //單量-盤後時交易量(14:30)

    private String a;   //五檔賣:590.0000_591.0000_592.0000_593.0000_594.0000_
    private String f;   //五檔賣對應委託數量:461_98_150_417_251_
    private String b;   //五檔買:589.0000_588.0000_587.0000_586.0000_585.0000_
    private String g;   //五檔買對應委託數量:9_1227_534_470_1228_

    private String d;   //日期:20210510
    private String t;   //當日盤中時間:13:30:00
    private String ot;  //當日盤後時間:14:30:00
    private String tlong;   //當日盤後時間轉為毫秒

    private String io;  //:RR
    private String ip;  //:0
    private String ts;  //:0
    private String mt;  //:000000
    private String i;   //:24
    private String it;  //:12
    private String p;   //:4
    private String ov;  //
    private String oz;  //
    private String ps;
    private String pz;
    private String oa;
    private String ob;

    private float increase;     //漲幅
    private float amplitude;    //振幅

    public float getIncrease() {
        //收盤價 - 開盤價 / 開盤價 * 100%，在四捨五入到第二位
        if (NumberUtils.isNumber(this.z) && NumberUtils.isNumber(this.y)) {
            BigDecimal openPrice = new BigDecimal(this.y);
            BigDecimal closePrice = new BigDecimal(this.z);
            BigDecimal result = ((closePrice.subtract(openPrice)).divide(openPrice, 5, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100l))).setScale(2, BigDecimal.ROUND_HALF_UP);
            return result.floatValue();
        }
        return -1f;
    }

    public float getAmplitude() {
        //當日最高價 - 當日最低價 / 當日平盤價 * 100%，在四捨五入到第二位
        if (NumberUtils.isNumber(this.h) && NumberUtils.isNumber(this.l)) {
            BigDecimal highestPrice = new BigDecimal(this.h);
            BigDecimal lowestPrice = new BigDecimal(this.l);
            BigDecimal platePrice = new BigDecimal(this.y);
            BigDecimal result = ((highestPrice.subtract(lowestPrice)).divide(platePrice, 5, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100l))).setScale(2, BigDecimal.ROUND_HALF_UP);
            return result.floatValue();
        }
        return -1f;
    }


}
