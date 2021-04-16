package com.eachnow.linebot.common.po;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatetimepickerPO {
    public static String TYPE_START = "start";
    public static String TYPE_END = "end";
    private String date;
    private String time;
    private String datetime;
    private String type;    //start、end
}
