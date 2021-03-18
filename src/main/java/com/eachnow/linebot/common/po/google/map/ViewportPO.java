package com.eachnow.linebot.common.po.google.map;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ViewportPO {
    private LocationPO northeast;
    private LocationPO southwest;
}
