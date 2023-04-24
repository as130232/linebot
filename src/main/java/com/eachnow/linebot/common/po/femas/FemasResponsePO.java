package com.eachnow.linebot.common.po.femas;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class FemasResponsePO {
    private String status;
    private String err_msg;
    private Boolean force_change_password;
    private String type;
    private String search_start;
    private String search_end;
    private String show_shift_count;
    private String show_otShift_count;
    private Integer show_outBack_count;
    private Boolean show_overtime_confirm;
    private Integer limit;
    private Boolean offset;
    private Boolean datas_count;
    private List<FemasDataPO> datas;
}
