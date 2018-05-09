package com.dongluhitec.iotweb.bean;

import com.huawei.iotplatform.client.dto.ClientInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConfigurationProperties(prefix = "client")
public class DongluClientInfo extends ClientInfo {
}
