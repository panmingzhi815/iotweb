package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class SetDateTime {

    public ObjectNode get() {
        Long timestamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000;

        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",Integer.parseInt("AA",16));
        jsonNodes.put("CMD",Integer.parseInt("11",16));
        jsonNodes.put("SEQ",timestamp.intValue());
        jsonNodes.put("LEN",13);
        jsonNodes.put("CURTIME",timestamp.intValue());
        jsonNodes.put("CRC",Integer.parseInt("F8",16));
        jsonNodes.put("STOP",Integer.parseInt("BB",16));

        return jsonNodes;
    }
}
