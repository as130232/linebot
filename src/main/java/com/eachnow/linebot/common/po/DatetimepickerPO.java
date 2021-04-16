package com.eachnow.linebot.common.po;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatetimepickerPO {
    private String date;
    private String time;
    private String datetime;
}
