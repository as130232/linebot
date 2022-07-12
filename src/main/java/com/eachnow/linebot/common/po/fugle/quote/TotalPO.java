package com.eachnow.linebot.common.po.fugle.quote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TotalPO {
    private String at;  //2021-05-03T05:30:00.000Z
    private Integer unit;   //總成交張數
    private Integer volume; //總成交量
}
