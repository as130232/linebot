package com.eachnow.linebot.common.po.twse;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TwseRatioAndDividendYieldPO {
    private String stat;
    private String date;
    private String title;
    private List<String> fields;
    private List<List<String>> data;
}
