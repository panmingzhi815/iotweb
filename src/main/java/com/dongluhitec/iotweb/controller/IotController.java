package com.dongluhitec.iotweb.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dongluhitec.iotweb.iot.AddCardCmd;
import com.dongluhitec.iotweb.bean.CommandReq;
import com.dongluhitec.iotweb.bean.CommandRes;
import com.dongluhitec.iotweb.bean.LoginUser;
import com.dongluhitec.iotweb.config.Caches;
import com.dongluhitec.iotweb.config.DongluAuthentication;
import com.dongluhitec.iotweb.iot.AddPasswordCmd;
import com.dongluhitec.iotweb.iot.DelCmd;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    public ResponseBody createLoginUser(@RequestBody LoginUser loginUser) {
        LoginUser save = loginUserRepository.save(loginUser);
        return ResponseBody.success("type", save.getId());
    }

    @DeleteMapping("/loginUser/{id}")
    public ResponseBody removeLoginUser(@PathVariable("id") Long id) {
        loginUserRepository.deleteById(id);
        return ResponseBody.success("type",id);
    }

    @PutMapping("/loginUser")
    public ResponseBody editLoginUser(@RequestBody LoginUser loginUser) {
        boolean existsById = loginUserRepository.existsById(loginUser.getId());
        if (existsById) {
            loginUserRepository.save(loginUser);
            return ResponseBody.success("type",loginUser.getId());
        }
        return ResponseBody.fail(100002,"id不存在");
    }

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
        QueryDevicesOutDTO queryDevicesOutDTO = dataCollection.queryDevices(queryDevicesInDTO, authentication.getNorthApiClient().getClientInfo().getAppId(), authentication.getAccessTokenString());
        queryDevicesOutDTO.getDevices().stream().forEach(e->{
            try {
                e.getDeviceInfo().setBatteryLevel(Caches.battery.get(e.getDeviceId(), () -> ""));
                e.getDeviceInfo().setSignalStrength(Caches.signal.get(e.getDeviceId(), () -> ""));
                e.getDeviceInfo().setStatusDetail(Caches.open.get(e.getDeviceId(), () -> ""));
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            }
        });
        return queryDevicesOutDTO;
    }

    @PostMapping("/device")
    public RegDirectDeviceOutDTO regDirectDevice(@RequestBody ModifyDeviceInfoInDTO modifyDeviceInfoInDTO) throws NorthApiException {
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
        return regDirectDeviceOutDTO;
    }

    @DeleteMapping("/device")
    public ResponseBody deleteDirectDevice(@RequestParam String deviceId) throws NorthApiException {
        DeviceManagement deviceManagement = new DeviceManagement(authentication.getNorthApiClient());
        deviceManagement.deleteDirectDevice(deviceId,authentication.getNorthApiClient().getClientInfo().getAppId(),authentication.getAccessTokenString());
        return ResponseBody.success("status",Boolean.TRUE);
    }

    @GetMapping("/device/open")
    public ResponseBody open(@RequestParam String deviceId) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("LockCtl");
        commandReq.setServiceId("RomteCtl");
        OpenCloseDoor openCloseDoor = new OpenCloseDoor(2);
        commandReq.setContent(openCloseDoor.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

    @GetMapping("/device/close")
    public ResponseBody close(@RequestParam String deviceId) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("LockCtl");
        commandReq.setServiceId("RomteCtl");
        OpenCloseDoor openCloseDoor = new OpenCloseDoor(0);
        commandReq.setContent(openCloseDoor.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

    @DeleteMapping("/device/{deviceId}/log")
    public ResponseBody deleteDeviceLog(@PathVariable("deviceId") String deviceId) {
        List<CommandRes> byDeviceId = commandResRepository.findByDeviceId(deviceId);
        assert byDeviceId != null;
        commandResRepository.deleteAll(byDeviceId);
        LOGGER.info("删除设备{}日志{}条",deviceId,byDeviceId.size());
        return ResponseBody.success("status",Boolean.TRUE);
    }

    @PostMapping("/device/log")
    public Page<CommandRes> getDeviceLog(@RequestBody QueryDataHistoryInDTO queryDataHistoryInDTO) {
        Page<CommandRes> commandResPage = commandResRepository.findAll((Specification<CommandRes>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!Strings.isNullOrEmpty(queryDataHistoryInDTO.getDeviceId())) {
                predicates.add(criteriaBuilder.like(root.get("deviceId"), queryDataHistoryInDTO.getDeviceId()));
            }
            if (!Strings.isNullOrEmpty(queryDataHistoryInDTO.getGatewayId())) {
                predicates.add(criteriaBuilder.like(root.get("gatewayId"), queryDataHistoryInDTO.getGatewayId()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, PageRequest.of(queryDataHistoryInDTO.getPageNo(), queryDataHistoryInDTO.getPageSize(), Sort.by(Sort.Direction.DESC, "createTime")));
        return commandResPage;
    }

    @PostMapping("/receiveLog")
    public ResponseBody receiveCommandRspNotify(HttpServletRequest request) {
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

    @PostMapping("/receiveCommand")
    public ResponseBody receiveCommand(HttpServletRequest request) {
        try (ServletInputStream inputStream = request.getInputStream()){
            final String log = IOUtils.toString(inputStream, "utf-8");
            LOGGER.info("接收到设备响应命令变化:{}",log);
        }catch (Exception io){
            LOGGER.error("处理设备数据命令变化订阅回传异常",io);
        }
        return ResponseBody.success("receive content is success:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @DeleteMapping("/command")
    public ResponseBody deleteAllCommand() {
        commandReqRepository.deleteAll();
        return ResponseBody.success("success");
    }

    private void downloadCmd(String deviceId, CommandReq commandReq) throws Exception {
        PostDeviceCommandInDTO postDeviceCommandInDTO = new PostDeviceCommandInDTO();
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

    @PostMapping("/password")
    public ResponseBody addPassword(@RequestParam(required = false) String password,@RequestParam String deviceId,@RequestParam Integer id,@RequestParam(required = false) String cardId) throws Exception {
        assert password.length() == 6;
        if (!Strings.isNullOrEmpty(password)) {
            downloadPassword(password,deviceId,id);
        }
        if (!Strings.isNullOrEmpty(cardId)) {
            downloadCard(deviceId, id, cardId);
        }
        return ResponseBody.success("success");
    }


    @DeleteMapping("/password")
    public ResponseBody delPassword(@RequestParam Integer id, @RequestParam String deviceId,@RequestParam Integer type) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("DelScCtl");
        commandReq.setServiceId("DelSC");
        DelCmd delCmd = new DelCmd(id,type);
        commandReq.setContent(delCmd.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

    /**
     * 同时下载卡片与密码
     * @param deviceId
     * @param id
     * @param cardId
     * @return
     */
    private ResponseBody downloadCard(String deviceId, Integer id, String cardId) throws Exception {
        cardId = cardId.replace('\u202D',' ').replace('\u202C',' ').trim();
        cardId = Strings.padStart(cardId,10,'0');
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("AddCard");
        commandReq.setServiceId("AddCard");
        AddCardCmd addCardCmd = new AddCardCmd(cardId,id);
        commandReq.setContent(addCardCmd.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

    /**
     * 单独下载密码
     * @param password
     * @param deviceId
     * @param id
     * @return
     */
    private ResponseBody downloadPassword(@RequestParam String password, @RequestParam String deviceId, @RequestParam Integer id) throws Exception {
        CommandReq commandReq = new CommandReq();
        commandReq.setDeviceId(deviceId);
        commandReq.setMethod("AddCtl");
        commandReq.setServiceId("AddSC");
        AddPasswordCmd passwordCmd = new AddPasswordCmd(password, id);
        commandReq.setContent(passwordCmd.get().toString());
        downloadCmd(deviceId,commandReq);
        return ResponseBody.success("success");
    }

}
