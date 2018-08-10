package com.dongluhitec.iotweb.config;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;

public class Caches {
    public static final Cache<String,String> battery =  CacheBuilder.newBuilder().build();
    public static final Cache<String,String> signal =  CacheBuilder.newBuilder().build();
    public static final Cache<String,String> open =  CacheBuilder.newBuilder().build();
    public static final Cache<String,Integer> time =  CacheBuilder.newBuilder().build();

    public static void updateCache(JSONObject jsonObject){
        String deviceId = jsonObject.getString("deviceId");
        JSONObject service = jsonObject.getJSONObject("service");

        if (deviceId != null && service != null ) {
            String serviceType = service.getString("serviceType");
            JSONObject data = service.getJSONObject("data");
            if("UpStates".equals(serviceType) && data != null){
                battery.put(deviceId,Optional.ofNullable(data.getInteger("BATTERY")).map(String::valueOf).orElse(""));
                signal.put(deviceId,Optional.ofNullable(data.getInteger("SIGNAL")).map(String::valueOf).orElse(""));
                open.put(deviceId,Optional.ofNullable(data.getInteger("LOCKSTATE")).map(m->m.equals(0)?"关":"开").map(String::valueOf).orElse(""));
                time.put(deviceId,Optional.ofNullable(data.getInteger("CurTime")).orElse(0));
            }
        }
    }

}
