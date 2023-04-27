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
public class FemasPunchRecordPO {
    private String date;
    private String punchIn;         // 打卡上班時間
    private String punchOut;        // 預期可打卡下班時間
    private String actualPunchOut;  // 最後一次打卡下班時間
}
