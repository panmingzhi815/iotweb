package com.dongluhitec.iotweb.config;

import com.dongluhitec.iotweb.bean.DongluClientInfo;
import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.AuthRefreshInDTO;
import com.huawei.iotplatform.client.dto.ClientInfo;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class DongluAuthentication extends Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DongluAuthentication.class);
    private static final ScheduledExecutorService refreshTokenService = Executors.newSingleThreadScheduledExecutor();
    private static Date lastTokenTime;

    @Bean
    public Authentication authentication(@Autowired DongluClientInfo dongluClientInfo){
        NorthApiClient northApiClient = new NorthApiClient();
        try {
            northApiClient.setClientInfo(dongluClientInfo);
            northApiClient.initSSLConfig();
        } catch (NorthApiException e) {
            LOGGER.error("配置iot参数异常",e);
        }

        Authentication authentication = new Authentication(northApiClient);
        refreshTokenService.scheduleWithFixedDelay(()-> refreshTokenTask(authentication),5,300,TimeUnit.SECONDS);
        return authentication;
    }

    private void refreshTokenTask(Authentication authentication) {
        try {
            LOGGER.debug("开始检查iot token");
            AuthOutDTO authToken = authentication.getAuthToken();
            if (authToken == null) {
                refreshToken(authentication);
                lastTokenTime = new Date();
                LOGGER.info("刷新iot token成功");
                return;
            }

            if (lastTokenTime == null) {
                lastTokenTime = new Date();
            }

            long last = lastTokenTime.getTime();
            long now = new Date().getTime();

            if (now - last > authToken.getExpiresIn() * 1000){
                refreshToken(authentication);
                lastTokenTime = new Date();
                LOGGER.info("刷新iot token成功");
            }
        } catch (Exception e) {
            LOGGER.error("系统自动刷新token时发生异常",e);
        }
    }

    private void refreshToken(Authentication authentication) throws NorthApiException {
        LOGGER.info("刷新iot token");
        ClientInfo clientInfo = authentication.getNorthApiClient().getClientInfo();
        AuthRefreshInDTO arid = new AuthRefreshInDTO();
        arid.setAppId(clientInfo.getAppId());
        arid.setSecret(clientInfo.getSecret());
        arid.setRefreshToken(Optional.ofNullable(authentication.getAuthToken()).map(AuthOutDTO::getRefreshToken).orElse(null));
        authentication.refreshAuthToken(arid);
    }
}
