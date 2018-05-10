package com.dongluhitec.iotweb.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dongluhitec.iotweb.bean.CommandRes;
import com.dongluhitec.iotweb.bean.LoginUser;
import com.dongluhitec.iotweb.repository.DeviceRepository;
import com.google.common.base.Strings;
import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.*;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.DataCollection;
import com.huawei.iotplatform.client.invokeapi.DeviceManagement;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.persistence.criteria.Predicate;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@EnableSwagger2
@RestController
public class IotController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IotController.class);

    @Autowired
    private Authentication authentication;
    @Autowired
    private DeviceRepository deviceRepository;

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

    @GetMapping("/device")
    public QueryDevicesOutDTO getDeviceList(@RequestParam Integer pageNo,@RequestParam Integer pageSize,@RequestParam(required = false) String gatewayId,@RequestParam(required = false) String status) throws NorthApiException {
        QueryDevicesInDTO queryDevicesInDTO = new QueryDevicesInDTO();
        queryDevicesInDTO.setPageNo(pageNo);
        queryDevicesInDTO.setPageSize(pageSize);
        queryDevicesInDTO.setGatewayId(Strings.emptyToNull(gatewayId));
        queryDevicesInDTO.setStatus(Strings.emptyToNull(status));
        DataCollection dataCollection = new DataCollection(authentication.getNorthApiClient());
        return dataCollection.queryDevices(queryDevicesInDTO, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAuthToken().getAccessToken());
    }

    @PostMapping("/device")
    public RegDirectDeviceOutDTO regDirectDevice(@RequestBody ModifyDeviceInfoInDTO modifyDeviceInfoInDTO) throws NorthApiException, InterruptedException {
        RegDirectDeviceInDTO rddid = new RegDirectDeviceInDTO();
        rddid.setNodeId(modifyDeviceInfoInDTO.getDeviceId());
        RegDirectDeviceOutDTO regDirectDeviceOutDTO =  new DeviceManagement(authentication.getNorthApiClient()).regDirectDevice(rddid, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAuthToken().getAccessToken());
        String manufacturerId = modifyDeviceInfoInDTO.getManufacturerId();
        String[] split = manufacturerId.split("/");

        modifyDeviceInfoInDTO.setDeviceId(regDirectDeviceOutDTO.getDeviceId());
        modifyDeviceInfoInDTO.setManufacturerId(split[0]);
        modifyDeviceInfoInDTO.setManufacturerName(split[1]);
        new DeviceManagement(authentication.getNorthApiClient()).modifyDeviceInfo(modifyDeviceInfoInDTO, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAuthToken().getAccessToken());
        return regDirectDeviceOutDTO;
    }

    @DeleteMapping("/device")
    public ResponseBody deleteDirectDevice(@RequestParam String deviceId) throws NorthApiException {
        DeviceManagement deviceManagement = new DeviceManagement(authentication.getNorthApiClient());
        deviceManagement.deleteDirectDevice(deviceId,authentication.getNorthApiClient().getClientInfo().getAppId(),authentication.getAuthToken().getAccessToken());
        return ResponseBody.success("status",Boolean.TRUE);
    }

    @PostMapping("/device/log")
    public Page<CommandRes> getDeviceLog(@RequestBody QueryDataHistoryInDTO queryDataHistoryInDTO) {
        return deviceRepository.findAll((Specification<CommandRes>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(queryDataHistoryInDTO.getDeviceId())) {
                predicates.add(criteriaBuilder.like(root.get("deviceId"), queryDataHistoryInDTO.getDeviceId()));
            }
            if (!Strings.isNullOrEmpty(queryDataHistoryInDTO.getGatewayId())) {
                predicates.add(criteriaBuilder.like(root.get("gatewayId"), queryDataHistoryInDTO.getGatewayId()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, PageRequest.of(queryDataHistoryInDTO.getPageNo(), queryDataHistoryInDTO.getPageSize()));
    }

    @PostMapping("/receiveLog")
    public String receiveCommandRspNotify(HttpServletRequest request) {
        try (ServletInputStream inputStream = request.getInputStream()){
            String log = IOUtils.toString(inputStream, "utf-8");
            LOGGER.info("接收到设备数据变化:{}",log);
            JSONObject jsonObject = JSON.parseObject(log);
            CommandRes commandRes = new CommandRes();
            commandRes.setDeviceId(jsonObject.getString("deviceId"));
            commandRes.setGatewayId(jsonObject.getString("gatewayId"));
            commandRes.setNotifyType(jsonObject.getString("notifyType"));
            commandRes.setService(jsonObject.getJSONObject("service").toJSONString());
            deviceRepository.save(commandRes);
        }catch (Exception io){
            LOGGER.error("处理设备数据变化订阅回传异常",io);
        }
        return "receive log is success:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @GetMapping("/subscribeCommandRspNotify")
    public String subscribeCommandRspNotify() throws NorthApiException {
        DataCollection dataCollection = new DataCollection(authentication.getNorthApiClient());
        SubscribeInDTO sid = new SubscribeInDTO();
        sid.setNotifyType("commandRsp");
        sid.setCallbackurl("http://103.46.128.47:19781/receiveLog");
        dataCollection.subscribeNotify(sid,authentication.getAuthToken().getAccessToken());
        return "register receive log success:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
