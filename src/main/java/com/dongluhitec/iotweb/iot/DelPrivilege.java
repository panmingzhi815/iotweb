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
public class DelPrivilege {
    private String card;

    public ObjectNode get() {
        card = Strings.padStart(Strings.nullToEmpty(card),16,'0');
        Long timestamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000;

        ObjectNode jsonNodes = new ObjectNode(JsonNodeFactory.instance);
        jsonNodes.put("STX",Integer.parseInt("AA",16));
        jsonNodes.put("CMD",9);
        jsonNodes.put("SEQ",timestamp.intValue());
        jsonNodes.put("LEN",17);
        jsonNodes.put("IDH",Long.parseLong(card.substring(0,8),16));
        jsonNodes.put("IDL",Long.parseLong(card.substring(8),16));
        jsonNodes.put("CRC",Integer.parseInt("F8",16));
        jsonNodes.put("STOP",Integer.parseInt("BB",16));

        return jsonNodes;
    }

}
