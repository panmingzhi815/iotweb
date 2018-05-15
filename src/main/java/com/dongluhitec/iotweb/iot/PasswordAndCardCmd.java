package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huawei.iotplatform.utils.JsonUtil;
import lombok.Data;

@Data
public class PasswordAndCardCmd{
    private String password;
    private String card;
    private String id;

    public PasswordAndCardCmd(String password, String card, String id) {
        this.password = password;
        this.card = card;
        this.id = id;
    }

    public ObjectNode get(){
        char[] passwordChars = password.toCharArray();
        char[] cardChars = card.toCharArray();
        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("CMD",6);
        jsonNodes.put("CARD_AUTHOR_TYPE",1);
        jsonNodes.put("CARD_NUM",1);
        jsonNodes.put("SEQ",Integer.valueOf(id));
        jsonNodes.put("LRC",0);
        jsonNodes.put("LEN",18);
        jsonNodes.put("STX",2);
        int index = 0;
        jsonNodes.put("Passwd0",Character.getNumericValue(passwordChars[index++]));
        jsonNodes.put("Passwd1",Character.getNumericValue(passwordChars[index++]));
        jsonNodes.put("Passwd2",Character.getNumericValue(passwordChars[index++]));
        jsonNodes.put("Passwd3",Character.getNumericValue(passwordChars[index++]));
        jsonNodes.put("Passwd4",Character.getNumericValue(passwordChars[index++]));
        jsonNodes.put("Passwd5",Character.getNumericValue(passwordChars[index]));
        index = 0;
        jsonNodes.put("idcard0",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard1",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard2",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard3",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard4",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard5",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard6",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard7",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard8",Character.getNumericValue(cardChars[index++]));
        jsonNodes.put("idcard9",Character.getNumericValue(cardChars[index]));
        return jsonNodes;
    }
}
