package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;


@Data
public class AddPasswordCmd {
    private final String password;
    private final Integer id;

    public AddPasswordCmd(String password, Integer id) {
        this.password = password;
        this.id = id;
    }

    public ObjectNode get() {
        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",2);
        jsonNodes.put("SEQ",id);
        jsonNodes.put("LRC",0);
        jsonNodes.put("LEN",13);
        jsonNodes.put("CMD",8);
        jsonNodes.put("ID",id);
        int index = 0;
        char[] passChars = password.toCharArray();
        jsonNodes.put("passwd0",Character.getNumericValue(passChars[index++]));
        jsonNodes.put("passwd1",Character.getNumericValue(passChars[index++]));
        jsonNodes.put("passwd2",Character.getNumericValue(passChars[index++]));
        jsonNodes.put("passwd3",Character.getNumericValue(passChars[index++]));
        jsonNodes.put("passwd4",Character.getNumericValue(passChars[index++]));
        jsonNodes.put("passwd5",Character.getNumericValue(passChars[index++]));

        return jsonNodes;
    }
}
