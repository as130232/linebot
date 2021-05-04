package com.eachnow.linebot.common.po.fugle.quote;

import com.eachnow.linebot.common.po.fugle.InfoPO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuoteDataPO {
    private InfoPO info;
    private QuotePO quote;
}
