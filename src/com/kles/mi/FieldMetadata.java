/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jchau
 */
public class FieldMetadata {

    private String description;

    private String name;

    private String length;

    private String type;

    public FieldMetadata() {
    }

    public FieldMetadata(String name, String desc, String type, String length) {
        this.name = name;
        this.description = desc;
        this.type = type;
        this.length = length;
    }

    @JsonProperty("@description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("@description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("@name")
    public String getName() {
        return name;
    }

    @JsonProperty("@name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("@length")
    public String getLength() {
        return length;
    }

    @JsonProperty("@length")
    public void setLength(String length) {
        this.length = length;
    }

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    @JsonProperty("@type")
    public void setType(String type) {
        this.type = type;
    }
}
