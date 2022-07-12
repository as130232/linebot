package com.eachnow.linebot.common.po.twse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TwseDataPO {
    private String stat;
    private String date;
    private String title;
    private List<String> fields;
    private List<List<String>> data;
}
