package com.dongluhitec.iotweb.controller;

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
@Controller
public class PageController {

    private final static Logger LOGGER = LoggerFactory.getLogger(PageController.class);

    @GetMapping(value = {"/index","/","/index.html"})
    public String index(Model model){
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

        return "index";
    }

    @GetMapping("/device.html")
    public String device() {
        return "device";
    }
}
