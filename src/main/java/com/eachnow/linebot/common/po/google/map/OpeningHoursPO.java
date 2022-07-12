package com.eachnow.linebot.common.po.google.map;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OpeningHoursPO {
    @JsonAlias("open_now")
    private Boolean openNow;
}
