package com.dongluhitec.iotweb.bean;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@ToString
public class CommandReq {
    @Id
    @GeneratedValue
    private Long id;
    private String deviceId;
    private String method;
    private String serviceId;
    @Column(length = 500)
    private String content;
    private Date createTime;

    @PrePersist
    public void PrePersist() {
        createTime = new Date();
    }
}
