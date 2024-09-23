package com.eachnow.linebot.common.po.femas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FemasPayResponsePO {
    private String status;
    private String err_msg;
    private boolean force_change_password;
    private List<FemasPayDataPO> datas;
    private Integer total;

    private Integer getTotal() {
        return datas.stream().mapToInt(data -> Integer.parseInt(data.getReceived().replace(",", "")))
                .sum();
    }
}
