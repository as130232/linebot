package com.eachnow.linebot.common.po.fugle.chart;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChartPricePO {
    private Integer open;
    private Integer high;
    private Integer low;
    private Integer close;
    private Integer volume;
    private Integer unit;
}
