package com.dongluhitec.iotweb.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dongluhitec.iotweb.bean.CommandReq;
import com.dongluhitec.iotweb.bean.CommandRes;
import com.dongluhitec.iotweb.bean.Device;
import com.dongluhitec.iotweb.bean.LoginUser;
import com.dongluhitec.iotweb.config.Caches;
import com.dongluhitec.iotweb.config.DongluAuthentication;
import com.dongluhitec.iotweb.iot.AddPrivilege;
import com.dongluhitec.iotweb.iot.DelPrivilege;
import com.dongluhitec.iotweb.iot.OpenCloseDoor;
import com.dongluhitec.iotweb.repository.CommandReqRepository;
import com.dongluhitec.iotweb.repository.CommandResRepository;
import com.dongluhitec.iotweb.repository.LoginUserRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.*;
import com.huawei.iotplatform.client.invokeapi.DataCollection;
import com.huawei.iotplatform.client.invokeapi.DeviceManagement;
import com.huawei.iotplatform.client.invokeapi.SignalDelivery;
import com.huawei.iotplatform.utils.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.execchain.MinimalClientExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.persistence.criteria.Predicate;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@EnableSwagger2
@RestController
public class IotController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IotController.class);

    private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();
    @Autowired
    private DongluAuthentication authentication;
    @Autowired
    private CommandResRepository commandResRepository;
    @Autowired
    private CommandReqRepository commandReqRepository;
    @Autowired
    private LoginUserRepository loginUserRepository;

    @PostMapping("/loginUser")
    public JSONObject createLoginUser(@RequestBody LoginUser loginUser) {
        LoginUser save = loginUserRepository.save(loginUser);
        return ResponseBody.success("type", save.getId());
    }

    @DeleteMapping("/loginUser/{id}")
    public JSONObject removeLoginUser(@PathVariable("id") Long id) {
        loginUserRepository.deleteById(id);
        return ResponseBody.success("type",id);
    }

    @PutMapping("/loginUser")
    public JSONObject editLoginUser(@RequestBody LoginUser loginUser) {
        boolean existsById = loginUserRepository.existsById(loginUser.getId());
        if (existsById) {
            loginUserRepository.save(loginUser);
            return ResponseBody.success("type",loginUser.getId());
        }
        return ResponseBody.fail(100002,"id不存在");
    }

    @PostMapping("/session")
    public JSONObject login(@RequestBody LoginUser loginUser,HttpSession httpSession){
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
    public JSONObject getDeviceList(@RequestParam Integer page,@RequestParam Integer limit,@RequestParam(required = false) String deviceId,@RequestParam(required = false) String status) throws NorthApiException {
        QueryDevicesInDTO in = new QueryDevicesInDTO();
        in.setPageNo(page - 1);
        in.setPageSize(limit);
        in.setGatewayId(Strings.emptyToNull(deviceId));
        in.setStatus(Strings.emptyToNull(status));
        DataCollection dataCollection = new DataCollection(authentication.getNorthApiClient());
        QueryDevicesOutDTO out = dataCollection.queryDevices(in, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAccessTokenString());
        List<Device> deviceList = out.getDevices().stream().map(m -> {
            Device device = new Device();
            try {
                device.setDeviceId(m.getDeviceId());
                device.setNodeId(m.getDeviceInfo().getNodeId());
                device.setStatus(m.getDeviceInfo().getStatus());
                device.setDeviceType(m.getDeviceInfo().getDeviceType());
                device.setName(m.getDeviceInfo().getName());
                device.setBatteryLevel(Caches.battery.get(m.getDeviceId(), () -> ""));
                device.setSignalStrength(Caches.signal.get(m.getDeviceId(), () -> ""));
                device.setStatusDetail(Caches.open.get(m.getDeviceId(), () -> ""));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return device;
        }).collect(Collectors.toList());
        return ResponseBody.successPageList(out.getTotalCount(),deviceList);
    }
    /**
     * 注册北向平台设备
     * @param modifyDeviceInfoInDTO 修改信息
     * @return json
     * @throws NorthApiException exception
     */
    @PostMapping("/device")
    public JSONObject regDirectDevice(@RequestBody ModifyDeviceInfoInDTO modifyDeviceInfoInDTO) throws NorthApiException, IOException {
        String deviceId = modifyDeviceInfoInDTO.getDeviceId();

        RegDirectDeviceInDTO deviceInDTO = new RegDirectDeviceInDTO();
        deviceInDTO.setNodeId(deviceId);
        deviceInDTO.setVerifyCode(deviceId);
        deviceInDTO.setTimeout(0);

        LOGGER.info("设备注册：{}",deviceInDTO);
        DeviceManagement deviceManagement = new DeviceManagement(authentication.getNorthApiClient());
        RegDirectDeviceOutDTO regDirectDeviceOutDTO =  deviceManagement.regDirectDevice(deviceInDTO, authentication.getAppId(), authentication.getAccessTokenString());
        LOGGER.info("设备注册结果：{}",regDirectDeviceOutDTO);

        QueryDeviceStatusOutDTO statusOutDTO = deviceManagement.queryDeviceStatus(regDirectDeviceOutDTO.getDeviceId(), authentication.getAppId(), authentication.getAccessTokenString());
        LOGGER.info("设备状态：{}",statusOutDTO);

        String manufacturerId = modifyDeviceInfoInDTO.getManufacturerId();
        String[] split = manufacturerId.split("/");

        modifyDeviceInfoInDTO.setDeviceId(regDirectDeviceOutDTO.getDeviceId());
        modifyDeviceInfoInDTO.setManufacturerId(split[0]);
        modifyDeviceInfoInDTO.setManufacturerName(split[1]);
        modifyDeviceInfoInDTO.setProtocolType("CoAP");
        LOGGER.info("设备修改：{}",modifyDeviceInfoInDTO);

        deviceManagement.modifyDeviceInfo(modifyDeviceInfoInDTO, authentication.getAppId(), authentication.getAccessTokenString());

        return ResponseBody.success("success");
    }

    /**
     * 删除北向平台设备
     * @param deviceIds 设备id
     * @return json
     * @throws NorthApiException exception
     */
    @DeleteMapping("/device")
    public JSONObject deleteDirectDevice(@RequestParam String deviceIds) throws NorthApiException {
        assert deviceIds.length() > 0;
        String[] split = deviceIds.split(",");
        for (String deviceId : split) {
            DeviceManagement deviceManagement = new DeviceManagement(authentication.getNorthApiClient());
            deviceManagement.deleteDirectDevice(deviceId,authentication.getNorthApiClient().getClientInfo().getAppId(),authentication.getAccessTokenString());
        }
        return ResponseBody.success("status",Boolean.TRUE);
    }

    /**
     * 发送北向平台开锁命令
     * @param deviceId 设备id
     * @return json
     * @throws Exception exception
     */
    @GetMapping("/device/open")
    public JSONObject open(@RequestParam String deviceId) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("RemoteCTL");
        commandReq.setServiceId("RemoteCTL");
        OpenCloseDoor openCloseDoor = new OpenCloseDoor(1);
        commandReq.setContent(openCloseDoor.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

    /**
     * 发送北向平台关锁命令
     * @param deviceId 设备id
     * @return json
     * @throws Exception exception
     */
    @GetMapping("/device/close")
    public JSONObject close(@RequestParam String deviceId) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("RemoteCTL");
        commandReq.setServiceId("RemoteCTL");
        OpenCloseDoor openCloseDoor = new OpenCloseDoor(0);
        commandReq.setContent(openCloseDoor.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

    /**
     * 清空设备日志列表
     * @param deviceId 设备id
     * @return json
     */
    @DeleteMapping("/device/{deviceId}/log")
    public JSONObject deleteDeviceLog(@PathVariable("deviceId") String deviceId) {
        List<CommandRes> byDeviceId = commandResRepository.findByDeviceId(deviceId);
        assert byDeviceId != null;
        commandResRepository.deleteAll(byDeviceId);
        LOGGER.info("删除设备{}日志{}条",deviceId,byDeviceId.size());
        return ResponseBody.success("status",Boolean.TRUE);
    }

    /**
     * 获取设备日志列表
     * @return json
     */
    @GetMapping("/device/log")
    public JSONObject getDeviceLog(@RequestParam Integer page,@RequestParam Integer limit,@RequestParam(required = false) String deviceId) {
        Page<CommandRes> all = commandResRepository.findAll((Specification<CommandRes>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(deviceId)) {
                predicates.add(criteriaBuilder.like(root.get("deviceId"), deviceId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC, "createTime")));
        return ResponseBody.successPageList(all.getTotalElements(),all.getContent());
    }

    /**
     * 接收北向平台设备日志
     * @param request request
     * @return json
     */
    @PostMapping("/receiveLog")
    public JSONObject receiveCommandRspNotify(HttpServletRequest request) {
        try (ServletInputStream inputStream = request.getInputStream()){
            final String log = IOUtils.toString(inputStream, "utf-8");
            LOGGER.info("接收到设备数据变化:{}",log);
            logExecutor.submit(()->{
                JSONObject jsonObject = JSON.parseObject(log);
                final CommandRes commandRes = new CommandRes();
                commandRes.setDeviceId(jsonObject.getString("deviceId"));
                commandRes.setGatewayId(jsonObject.getString("gatewayId"));
                commandRes.setNotifyType(jsonObject.getString("notifyType"));
                commandRes.setServiceId(jsonObject.getJSONObject("service").getString("serviceId"));
                JSONObject dataJSON = jsonObject.getJSONObject("service").getJSONObject("data");
                String dataJSONString = JSON.toJSONString(dataJSON, SerializerFeature.SortField);
                commandRes.setData(dataJSONString);
                commandResRepository.save(commandRes);
                Caches.updateCache(jsonObject);
            });
        }catch (Exception io){
            LOGGER.error("处理设备数据变化订阅回传异常",io);
        }
        return ResponseBody.success("receive log is success:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 订阅北向平台响应命令
     * @param request request
     * @return json
     */
    @PostMapping("/receiveCommand")
    public JSONObject receiveCommand(HttpServletRequest request) {
        try (ServletInputStream inputStream = request.getInputStream()){
            final String log = IOUtils.toString(inputStream, "utf-8");
            LOGGER.info("接收到设备响应命令变化:{}",log);
        }catch (Exception io){
            LOGGER.error("处理设备数据命令变化订阅回传异常",io);
        }
        return ResponseBody.success("receive content is success:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 删除所有响应命令日志
     * @return json
     */
    @DeleteMapping("/command")
    public JSONObject deleteAllCommand() {
        commandReqRepository.deleteAll();
        return ResponseBody.success("success");
    }

    private void downloadCmd(String deviceId, CommandReq commandReq) throws Exception {
        PostDeviceCommandInDTO postDeviceCommandInDTO = new PostDeviceCommandInDTO();
        postDeviceCommandInDTO.setExpireTime(10 * 60);
        postDeviceCommandInDTO.setDeviceId(deviceId);
        AsynCommandDTO command = new AsynCommandDTO();
        postDeviceCommandInDTO.setCommand(command);
        command.setMethod(commandReq.getMethod());
        command.setServiceId(commandReq.getServiceId());
        ObjectNode jsonNodes = JsonUtil.convertObject2ObjectNode(commandReq.getContent());
        command.setParas(jsonNodes);
        LOGGER.info("应用平台主动下载命令{}到设备{}",postDeviceCommandInDTO,deviceId);
        SignalDelivery signalDelivery = new SignalDelivery(authentication.getNorthApiClient());
        PostDeviceCommandOutDTO postDeviceCommandOutDTO = signalDelivery.postDeviceCommand(postDeviceCommandInDTO, authentication.getAppId(), authentication.getAccessTokenString());
        LOGGER.info("应用平台己下载命令{}到设备{}",postDeviceCommandOutDTO,deviceId);
    }

    @GetMapping("/subscribeCommandRspNotify")
    public String subscribeCommandRspNotify(@RequestParam String notifyType,@RequestParam String callbackUrl) throws NorthApiException {
        DataCollection dataCollection = new DataCollection(authentication.getNorthApiClient());
        SubscribeInDTO sid = new SubscribeInDTO();
        sid.setNotifyType(notifyType);
        sid.setCallbackurl(callbackUrl);
        dataCollection.subscribeNotify(sid,authentication.getAuthToken().getAccessToken());
        return "register receive log success:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @PostMapping("/card")
    public JSONObject addCard(@RequestParam(required = false) String deviceId, @RequestParam(required = false) String card) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("DownPwr");
        commandReq.setServiceId("DownPwr");
        AddPrivilege addPrivilege = new AddPrivilege(null, card, null, null);
        commandReq.setContent(addPrivilege.get().toString());
        downloadCmd(deviceId,commandReq);

        return ResponseBody.success("success");
    }

    @DeleteMapping("/card")
    public JSONObject delelteCard(@RequestParam(required = false) String deviceId, @RequestParam(required = false) String card) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("DelOnePwr");
        commandReq.setServiceId("DleOnePwr");
        DelPrivilege delCmd = new DelPrivilege(card);
        commandReq.setContent(delCmd.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

}
