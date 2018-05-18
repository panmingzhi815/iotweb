package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class DelCmd {
    private Integer id;
    private Integer type;

    public DelCmd(Integer id,Integer type) {
        this.id = id;
        this.type = type;
    }

    public ObjectNode get(){
        ObjectNode jsonNodes = new com.fasterxml.jackson.databind.node.ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",2);
        jsonNodes.put("SEQ",id);
        jsonNodes.put("LRC",0);
        jsonNodes.put("LEN",4);
        jsonNodes.put("CMD",10);
        jsonNodes.put("FUNCTION",1);
        jsonNodes.put("TYPE",type);
        jsonNodes.put("SUM",1);
        jsonNodes.put("ID",id);
        return jsonNodes;
    }
}
