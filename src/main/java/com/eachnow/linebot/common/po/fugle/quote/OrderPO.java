package com.eachnow.linebot.common.po.fugle.quote;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderPO {
    private String at;  //2021-05-03T05:30:00.000Z
    private List<PricePO> bestBids;
    private List<PricePO> bestAsks;
    private PricePO priceHigh;
    private PricePO priceLow;
    private PricePO priceOpen;
}
