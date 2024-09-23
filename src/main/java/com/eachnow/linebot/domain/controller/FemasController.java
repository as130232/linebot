package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.db.po.LineUserPO;
import com.eachnow.linebot.common.po.Result;
import com.eachnow.linebot.common.po.femas.FemasPunchRecordPO;
import com.eachnow.linebot.common.util.DateUtils;
import com.eachnow.linebot.domain.service.FemasService;
import com.eachnow.linebot.domain.service.line.LineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/femas")
public class FemasController {
    private final FemasService femasService;
    private final LineUserService lineUserService;

    @Autowired
    public FemasController(FemasService femasService, LineUserService lineUserService) {
        this.femasService = femasService;
        this.lineUserService = lineUserService;
    }

    @GetMapping(value = "/punch/records")
    public Result<Map<String, FemasPunchRecordPO>> punchRecords(@RequestParam(value = "date", required = false) String date) {
        date = Objects.isNull(date) ? DateUtils.getCurrentDate() : date;
        Result<Map<String, FemasPunchRecordPO>> result = new Result<>();
        Map<String, FemasPunchRecordPO> data = femasService.getRecordAndSetRemind(date);
        result.setData(data);
        return result;
    }

    @GetMapping(value = "/punch/record")
    public Result<FemasPunchRecordPO> punchRecord(@RequestParam(value = "date", required = false) String date,
                                                  @RequestParam(value = "lineId") String lineId) {
        date = Objects.isNull(date) ? DateUtils.getCurrentDate() : date;
        Result<FemasPunchRecordPO> result = new Result<>();
        Optional<LineUserPO> optional = lineUserService.getUser(lineId);
        if (!optional.isPresent()) {
            result.setCode(Result.NOT_FOUND);
            return result;
        }
        LineUserPO user = optional.get();
        FemasPunchRecordPO po = femasService.getFemasPunchRecord(date, user.getName(), user.getFemasToken());
        if (po == null) {
            result.setCode(Result.NOT_FOUND);
            return result;
        }
        result.setData(po);
        return result;
    }
}
