package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.huawei.iotplatform.utils.JsonUtil;
import lombok.Data;

@Data
public class AddCardCmd {
    private String card;
    private Integer id;

    public AddCardCmd(String card, Integer id) {
        this.card = card;
        this.id = id;
    }

    public ObjectNode get(){
        card = Strings.padStart(this.card, 20, '0');
        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",2);
        jsonNodes.put("SEQ",id);
        jsonNodes.put("LRC",0);
        jsonNodes.put("LEN",11);
        jsonNodes.put("CMD",13);
        jsonNodes.put("ID",id);
        int index = 0;
        jsonNodes.put("Card9",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card8",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card7",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card6",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card5",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card4",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card3",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card2",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card1",Integer.parseInt(this.card.substring(index,index = index+2),16));
        jsonNodes.put("Card0",Integer.parseInt(this.card.substring(index,index = index+2),16));
        return jsonNodes;
    }
}
