package com.dongluhitec.iotweb.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBody {
    private Integer errorCode;
    private String errorMsg;
    private Object data;

    public static JSONObject success(Object o) {
        JSONObject result = new JSONObject();
        result.put("errorCode", 0);
        result.put("errorMssg", "success");
        return result;
    }

    public static JSONObject fail(int code, String msg) {
        JSONObject result = new JSONObject();
        result.put("errorCode", code);
        result.put("errorMssg", msg);
        return result;
    }

    public static JSONObject success(String key, Object value) {
        return success(new HashMap.SimpleEntry<>(key, value));
    }

    public static JSONObject successPageList(Long totalCount, Object object) {
        JSONObject result = new JSONObject();
        result.put("errorCode", 0);
        result.put("errorMssg", "success");
        result.put("total", totalCount);
        result.put("rows", object);
        return result;
    }
}
