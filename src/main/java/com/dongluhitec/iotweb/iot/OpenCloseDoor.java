package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class OpenCloseDoor {
    private Integer type;

    public OpenCloseDoor(Integer type) {
        this.type = type;
    }

    public ObjectNode get(){
        Long timestamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000;

        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",Integer.parseInt("AA",16));
        jsonNodes.put("CMD",2);
        jsonNodes.put("SEQ", timestamp.intValue());
        jsonNodes.put("LEN",18);
        jsonNodes.put("OPEN",type);
        jsonNodes.put("IDH",Integer.parseInt("00000000",16));
        jsonNodes.put("IDL",Integer.parseInt("00000000",16));
        jsonNodes.put("CRC",Integer.parseInt("F8",16));
        jsonNodes.put("STOP",Integer.parseInt("BB",16));
        return jsonNodes;
    }
}
