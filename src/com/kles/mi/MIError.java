/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author JCHAUT
 */
public class MIError {

    private String cfg;
    private String code;
    private String field;
    private String type;
    private String message;

    @JsonProperty("@cfg")
    public String getCfg() {
        return cfg;
    }

    @JsonProperty("@cfg")
    public void setCfg(String cfg) {
        this.cfg = cfg;
    }
    
    @JsonProperty("@code")
    public String getCode() {
        return code;
    }

    @JsonProperty("@code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("@field")
    public String getField() {
        return field;
    }

    @JsonProperty("@field")
    public void setField(String code) {
        this.field = code;
    }

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    @JsonProperty("@type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("Message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("Message")
    public void setMessage(String message) {
        this.message = message;
    }
}
