package com.eachnow.linebot.common.po.fugle.quote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TradePO {
    private String at;  //2021-05-03T05:30:00.000Z
    private Integer price;
    private Integer unit;
    private Integer volume;
    private String serial;
}
