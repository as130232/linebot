package com.eachnow.linebot.common.po.openweather;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParameterPO {
    private String parameterName;
    private String parameterValue;
    private String parameterUnit;
}
