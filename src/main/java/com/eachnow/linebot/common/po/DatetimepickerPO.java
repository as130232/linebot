package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class DatetimepickerPO {
    private String date;
    private String time;
    private String datetime;
}
