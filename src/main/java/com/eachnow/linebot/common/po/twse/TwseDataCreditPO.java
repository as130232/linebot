package com.eachnow.linebot.common.po.twse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TwseDataCreditPO {
    private String stat;
    private String date;
    private String creditTitle;
    private List<String> creditFields;
    private List<List<String>> creditList;
}
