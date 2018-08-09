package com.dongluhitec.iotweb.config;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.huawei.iotplatform.client.dto.PostDeviceCommandInDTO;
import com.huawei.iotplatform.client.invokeapi.SignalDelivery;
import io.swagger.models.auth.In;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

public class Caches {
    public static final Cache<String,String> battery =  CacheBuilder.newBuilder().build();
    public static final Cache<String,String> signal =  CacheBuilder.newBuilder().build();
    public static final Cache<String,String> open =  CacheBuilder.newBuilder().build();
    public static final Map<String, LinkedBlockingQueue<PostDeviceCommandInDTO>> deviceCommands = new HashMap<>();

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
            }
        }
    }

    public static LinkedBlockingQueue<PostDeviceCommandInDTO> getCommandTask(String deviceId) {
        if (!deviceCommands.containsKey(deviceId)) {
            deviceCommands.put(deviceId, new LinkedBlockingQueue<>());
        }
        return deviceCommands.get(deviceId);
    }
}
