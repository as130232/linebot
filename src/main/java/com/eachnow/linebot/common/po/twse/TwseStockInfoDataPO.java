package com.eachnow.linebot.common.po.twse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TwseStockInfoDataPO {
    private List<MsgPO> msgArray;   //詳細股價資訊
    private String referer;
    private String userDelay;
    private String rtcode;
    private QueryTimePO QueryTimeObject;
    private String rtmessage;
    private String exKey;
    private String cachedAlive;
}
