package com.eachnow.linebot.common.po.fugle.quote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PricePO {
    private Integer price;
    private Integer unit;
    private Integer volume;

    private String at;
}
