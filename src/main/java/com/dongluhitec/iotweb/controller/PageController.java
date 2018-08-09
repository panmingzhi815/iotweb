package com.dongluhitec.iotweb.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    private final static Logger LOGGER = LoggerFactory.getLogger(PageController.class);

    private String[] deviceTypes = {"DoorLock"};
    private String[] manufacturers = {"DongluHitec"};

    @GetMapping(value = {"/welcome.html"})
    public String welcome(HttpServletRequest httpServletRequest,Model model){
        model.addAttribute("serverName",httpServletRequest.getServerName());

        Properties properties = System.getProperties();
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            String key = ((String) entry.getKey()).replace(".","_");
            String value = (String)entry.getValue();
            model.addAttribute(key,value);
        }

        OperatingSystemMXBean mem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        model.addAttribute("total_ram",mem.getTotalPhysicalMemorySize() / 1024 / 1024 + "MB");
        model.addAttribute("available_ram",mem.getFreePhysicalMemorySize() / 1024 / 1024 + "MB");

        return "welcome";
    }

    @GetMapping(value = {"/index.html"})
    public String index(){
        return "index";
    }

    @GetMapping("/device.html")
    public String device() {
        return "device";
    }

    @GetMapping("/log.html")
    public String log(@RequestParam String deviceId, Model model) {
        model.addAttribute("deviceId",deviceId);
        return "log";
    }

    @GetMapping("/device_password.html")
    public String devicePassword() {
        return "device_password";
    }

    @GetMapping("/device_card.html")
    public String deviceCard(@RequestParam String deviceId,@RequestParam String nodeId,Model model) {
        model.addAttribute("deviceId", deviceId);
        model.addAttribute("nodeId", nodeId);
        return "device_card";
    }

    @GetMapping("/device_add.html")
    public String deviceAdd(Model model) {
        model.addAttribute("deviceTypes",deviceTypes);
        model.addAttribute("companys", manufacturers);
        return "device_add";
    }

    @GetMapping(value = {"/login","/login.html","/"})
    public String login(){
        return "login";
    }
}
