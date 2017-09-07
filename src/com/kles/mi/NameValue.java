/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author jchau
 */
public class NameValue implements Serializable {

    private String Name;

    private String Value;

    public NameValue() {
    }

    public NameValue(String key, String value) {
        this.Name = key;
        this.Value = value;
    }

    @JsonProperty("Name")
    public String getName() {
        return Name;
    }

    @JsonProperty("Name")
    public void setName(String Name) {
        this.Name = Name;
    }

    @JsonProperty("Value")
    public String getValue() {
        return Value;
    }

    @JsonProperty("Value")
    public void setValue(String Value) {
        this.Value = Value;
    }

    @Override
    public String toString() {
        return "ClassPojo [Name = " + Name + ", Value = " + Value + "]";
    }
}
