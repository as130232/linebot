package com.eachnow.linebot.common.po.fugle.dealts;

import com.eachnow.linebot.common.po.fugle.InfoPO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DealtsDataPO {
    private InfoPO info;
    private List<DealtsPO> dealts;
}
