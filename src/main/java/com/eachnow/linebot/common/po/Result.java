package com.eachnow.linebot.common.po;

import com.google.common.collect.Maps;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * RESTFUL return type
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "OK";
    public static final int FAILED_CODE = 1;
    public static final String FAILED_MSG = "failed";
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int ENCRYPT_ERR = 500;
    public static final int DECRYPT_ERR = 501;
    public static final int DECRYPT__METHOD_ERR = 502;

    private String msg = SUCCESS_MSG;
    private int code = SUCCESS_CODE;
    private T data;

    public Result() {
        super();
    }

    public Result(T data, String msg, int code) {
        super();
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public Result(String msg, int code) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public Result(T data) {
        super();
        this.data = data;
    }

    public static Result<Map> getDefaultResponse() {
        Result<Map> result = new Result<>();
        result.setData(Maps.newHashMap());
        return result;
    }
}

