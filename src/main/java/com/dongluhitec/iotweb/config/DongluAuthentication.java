package com.dongluhitec.iotweb.config;

import com.dongluhitec.iotweb.iot.DongluClientInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Configuration
public class DongluAuthentication extends Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DongluAuthentication.class);
    public static final Cache<String,String> accessToken =  CacheBuilder.newBuilder().expireAfterWrite(3,TimeUnit.HOURS).build();

    @Bean
    public DongluAuthentication authentication(@Autowired DongluClientInfo dongluClientInfo){
        NorthApiClient northApiClient = new NorthApiClient();
        try {
            northApiClient.setClientInfo(dongluClientInfo);
            northApiClient.initSSLConfig();
        } catch (NorthApiException e) {
            LOGGER.error("配置iot参数异常",e);
        }

        DongluAuthentication authentication = new DongluAuthentication();
        authentication.setNorthApiClient(northApiClient);
        return authentication;
    }

    public String getAccessTokenString() throws NorthApiException {
        try {
            return DongluAuthentication.accessToken.get("accessToken", () -> {
                String token = getAuthToken().getAccessToken();
                accessToken.put("accessToken",token);
                LOGGER.info("刷新token:{}",token);
                return token;
            });
        } catch (ExecutionException e) {
            throw new NorthApiException(e);
        }
    }

    public String getAppId(){
        return getNorthApiClient().getClientInfo().getAppId();
    }
}
