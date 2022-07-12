package com.eachnow.linebot.common.po.fugle;

import com.eachnow.linebot.common.po.fugle.dealts.DealtsDataPO;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class FugleDealtsPO {
    private String apiVersion;
    private DealtsDataPO data;
}
