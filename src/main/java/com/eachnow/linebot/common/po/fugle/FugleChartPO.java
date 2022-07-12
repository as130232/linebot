package com.eachnow.linebot.common.po.fugle;

import com.eachnow.linebot.common.po.fugle.chart.ChartDataPO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FugleChartPO {
    private String apiVersion;
    private ChartDataPO data;
}
