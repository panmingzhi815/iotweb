package com.dongluhitec.iotweb.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LayuiResponse {
    private int code;
    private String msg;
    private long count;
    private Object data;
}
