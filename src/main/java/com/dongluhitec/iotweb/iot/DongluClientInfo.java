package com.dongluhitec.iotweb.iot;

import com.huawei.iotplatform.client.dto.ClientInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client")
public class DongluClientInfo extends ClientInfo {
}
