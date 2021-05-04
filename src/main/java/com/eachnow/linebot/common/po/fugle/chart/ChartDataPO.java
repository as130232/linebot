package com.eachnow.linebot.common.po.fugle.chart;

import com.eachnow.linebot.common.po.fugle.InfoPO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ChartDataPO {
    private InfoPO info;
    private Map<String, ChartPricePO> chart;
}
