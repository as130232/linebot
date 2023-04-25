package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.LocalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/femas")
public class FemasController {
    private final LocalCacheService localCacheService;

    @Autowired
    public FemasController(LocalCacheService localCacheService) {
        this.localCacheService = localCacheService;
    }

    @GetMapping(value = "/punch/record")
    public Result<FemasPunchRecordPO> switchCron(@RequestParam(value = "date", required = false) String date) {
        date = Objects.isNull(date) ? DateUtils.getCurrentDate() : date;
        Result<FemasPunchRecordPO> result = new Result<>();
        result.setData(localCacheService.getPunchRecord(date));
        return result;
    }
}
