package com.eachnow.linebot.common.po.google.map;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class PlusCodePO {
    @JsonAlias("compound_code")
    private String compoundCode;
    @JsonAlias("global_code")
    private String globalCode;
}
