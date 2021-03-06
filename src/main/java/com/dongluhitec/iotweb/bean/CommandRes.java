package com.dongluhitec.iotweb.bean;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.util.Date;

@Data
@ToString
@Entity
public class CommandRes {
    @Id
    @GeneratedValue
    private Long id;
    private String notifyType;
    private String deviceId;
    private String gatewayId;
    private String requestId;
    private String serviceId;
    private String data;
    private Date createTime;

    @PrePersist
    public void preSave(){
        createTime = new Date();
    }

}
