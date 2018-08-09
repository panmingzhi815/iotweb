package com.dongluhitec.iotweb.iot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@AllArgsConstructor
public class AddPrivilege {

    private String password;
    private String card;
    private LocalDateTime validStart;
    private LocalDateTime validEnd;

    public ObjectNode get() {
        card = Strings.padStart(Strings.nullToEmpty(card),16,'0');
        password = Strings.padStart(Strings.nullToEmpty(password),8,'0');
        if (validStart == null) {
            validStart = LocalDateTime.now();
        }
        if (validEnd == null) {
            validEnd = LocalDateTime.now().plusYears(3);
        }
        Long timestamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000;

        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",Integer.parseInt("AA",16));
        jsonNodes.put("CMD",4);
        jsonNodes.put("SEQ",timestamp.intValue());
        jsonNodes.put("LEN",30);
        jsonNodes.put("IDH",Long.parseLong(card.substring(0,8),16));
        jsonNodes.put("IDL",Long.parseLong(card.substring(8),16));
        jsonNodes.put("PASSWD",Long.parseLong(password,16));
        jsonNodes.put("TIME_ST",validStart.toInstant(ZoneOffset.of("+8")).toEpochMilli()/1000);
        jsonNodes.put("TIME_SP",validEnd.toInstant(ZoneOffset.of("+8")).toEpochMilli()/1000);
        jsonNodes.put("WEEKID",11);
        jsonNodes.put("CRC",Integer.parseInt("F8",16));
        jsonNodes.put("STOP",Integer.parseInt("BB",16));

        return jsonNodes;
    }
}
