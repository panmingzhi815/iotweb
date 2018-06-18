package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import lombok.Data;

@Data
public class OpenCloseDoor {
    private Integer type;

    public OpenCloseDoor(Integer type) {
        this.type = type;
    }

    public ObjectNode get(){
        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",2);
        jsonNodes.put("SEQ", 1);
        jsonNodes.put("LRC",0);
        jsonNodes.put("LEN",2);
        jsonNodes.put("CMD",3);
        jsonNodes.put("OPEN",type);
        jsonNodes.put("AUTO_CLOSE_DELAY",3);
        return jsonNodes;
    }
}
