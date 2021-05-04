package com.eachnow.linebot.common.po.fugle;

import com.eachnow.linebot.common.po.fugle.quote.QuoteDataPO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FugleQuotePO {
    private String apiVersion;
    private QuoteDataPO data;
}
