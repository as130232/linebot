package com.eachnow.linebot.common.po.currency;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
public class ExratePO {
    private BigDecimal Exrate;
    private String UTC;
}
