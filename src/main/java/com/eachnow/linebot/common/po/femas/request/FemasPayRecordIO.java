package com.eachnow.linebot.common.po.femas.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FemasPayRecordIO {
    private String yearMonth;
}
