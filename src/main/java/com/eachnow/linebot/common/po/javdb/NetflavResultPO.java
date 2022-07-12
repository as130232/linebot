package com.eachnow.linebot.common.po.javdb;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NetflavResultPO {
    private List<NetflavDocPO> docs;
    private Integer total;
    private Integer limit;
    private Integer page;
    private Integer pages;
}
