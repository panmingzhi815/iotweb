package com.dongluhitec.iotweb.controller;

import com.dongluhitec.iotweb.bean.LoginUser;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.QueryDataHistoryInDTO;
import com.huawei.iotplatform.client.dto.QueryDataHistoryOutDTO;
import com.huawei.iotplatform.client.dto.QueryDevicesInDTO;
import com.huawei.iotplatform.client.dto.QueryDevicesOutDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.DataCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@EnableSwagger2
@RestController
public class IotController {


    @Autowired
    private Authentication authentication;

    @PostMapping("/session")
    public ResponseBody login(@RequestBody LoginUser loginUser,HttpSession httpSession){
        if ("admin".equals(loginUser.getUsername()) && "123456".equals(loginUser.getPassword())){
            httpSession.setAttribute("sessionId",httpSession.getId());
            return ResponseBody.success("sessionId",httpSession.getId());
        }
        return ResponseBody.fail(10001,"用户名密码不匹配");
    }

    @GetMapping("/session")
    public Boolean isLogin(HttpSession httpSession){
        return httpSession.getAttribute("sessionId") != null;
    }

    @PostMapping("/device")
    public QueryDevicesOutDTO getDeviceList(@RequestBody QueryDevicesInDTO queryDevicesInDTO) throws NorthApiException {
        if ("".equals(queryDevicesInDTO.getGatewayId())){
            queryDevicesInDTO.setGatewayId(null);
        }
        if ("".equals(queryDevicesInDTO.getStatus())){
            queryDevicesInDTO.setStatus(null);
        }
        DataCollection dataCollection = new DataCollection(authentication.getNorthApiClient());
        return dataCollection.queryDevices(queryDevicesInDTO, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAuthToken().getAccessToken());
    }

    @PostMapping("/device/log")
    public QueryDataHistoryOutDTO getDeviceLog(@RequestBody QueryDataHistoryInDTO queryDataHistoryInDTO) throws NorthApiException {
        DataCollection dataCollection = new DataCollection(authentication.getNorthApiClient());
        return dataCollection.queryDataHistory(queryDataHistoryInDTO, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAuthToken().getAccessToken());
    }
}
