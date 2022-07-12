package com.eachnow.linebot.common.po.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRatePO {
    private BigDecimal price;
    private String rate;
    private BigDecimal priceHigh;
    private BigDecimal priceLow;
    private Integer income;
}
