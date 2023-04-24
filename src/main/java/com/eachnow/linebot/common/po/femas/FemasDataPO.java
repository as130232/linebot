package com.eachnow.linebot.common.po.femas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class FemasDataPO {
    private String att_date;
    private String state_event;
    private Boolean is_holiday;
    private String holiday_name;
    private String holiday_type;
    private String holiday_color;
    private Boolean show_shift;
    private String shift;
    List<FemasPeriodPO> shift_periods;
    private String first_label;
    private String first_in_midnight;
    private String first_in;
    private String first_out_midnight;
    private String first_out;
    private Integer late_time;
    private String all_unusual;
    private String leave_records;
    private String leave_hours;
    private String ot_records;
    private String ot_hours;
    private String att_apply_records;
    private String shift_type;
    private String rated_time;
    private String eff_att_time;
    private String real_eff_att_time;
}
