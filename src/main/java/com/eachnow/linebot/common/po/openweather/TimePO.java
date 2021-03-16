package com.eachnow.linebot.common.po.openweather;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimePO {
    private String startTime;
    private String endTime;
    private ParameterPO parameter;
}
