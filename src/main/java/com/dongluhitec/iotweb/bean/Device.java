package com.dongluhitec.iotweb.bean;

import lombok.Data;

@Data
public class Device {
    private String deviceId;
    private String nodeId;
    private String name;
    private String deviceType;
    private String batteryLevel;
    private String signalStrength;
    private String statusDetail;
    private String status;
}
