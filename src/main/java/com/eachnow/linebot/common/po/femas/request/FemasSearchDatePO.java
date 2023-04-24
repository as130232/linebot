package com.eachnow.linebot.common.po.femas.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FemasSearchDatePO {
    private String type;
    private String searchStart;
    private String searchEnd;
    private Integer offset;
}
