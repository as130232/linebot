package com.eachnow.linebot.domain.controller;

import com.eachnow.linebot.common.po.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/setting")
public class SettingController {


    /**
     * 取得排程開關狀態
     */
    @GetMapping(value = "/test")
    public Result getCron() {
        Result<String> result = new Result<>();
        result.setData("test by charles:" + new Date());
        return result;
    }
}
