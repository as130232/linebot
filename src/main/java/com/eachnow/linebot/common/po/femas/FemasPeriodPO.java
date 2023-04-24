package com.eachnow.linebot.common.po.femas;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class FemasPeriodPO {
    private String start_time;
    private String end_time;
    private Integer midnight;
}
