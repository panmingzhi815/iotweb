package com.dongluhitec.iotweb.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class ResponseBody {
    private Integer errorCode;
    private String errorMsg;
    private Object result;

    public static ResponseBody success(Object o) {
        return new ResponseBody(null, null, o);
    }

    public static ResponseBody fail(int code, String msg) {
        return new ResponseBody(code, msg, null);
    }

    public static ResponseBody success(String key, Object value) {
        return success(new HashMap.SimpleEntry<>(key, value));
    }
}
