package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class TradeValueInfoPO {
    private String title;
    private List<TradeValuePO> tradeValues;
}
