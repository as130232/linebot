package com.eachnow.linebot.common.po.femas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class FemasPayDataPO {
    private String name;
    private String provided;
    private List<FemasValuePO> plus;
    private List<FemasValuePO> deduction;
    private String sum_plus;
    private String sum_deduction;
    private String received;
    private String payment_msgs;
    private String note;
    private String note2;

}
