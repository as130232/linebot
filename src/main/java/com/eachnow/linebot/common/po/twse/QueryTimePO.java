package com.eachnow.linebot.common.po.twse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QueryTimePO {
    private String sysDate;
    private String stockInfoItem;
    private String stockInfo;
    private String sessionStr;
    private String sysTime;
    private boolean showChart;
    private String sessionFromTime;
    private String sessionLatestTime;
}
