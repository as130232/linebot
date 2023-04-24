package com.eachnow.linebot.common.po.femas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上下班Cache資訊
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FemasRecordPO {
    private String date;
    private String startTime;
    private String endTime;         // 預期可下班時間
    private String actualEndTime;   // 實際下班時間
}
