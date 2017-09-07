/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jchau
 */
public class MIRecord implements Serializable{

    private List<NameValue> NameValue;
    private String RowIndex;

    @JsonProperty("NameValue")
    public List<NameValue> getNameValue() {
        return NameValue;
    }

    @JsonProperty("NameValue")
    public void setNameValue(List<NameValue> NameValue) {
        this.NameValue = NameValue;
    }

    @JsonProperty("RowIndex")
    public String getRowIndex() {
        return RowIndex;
    }

    @JsonProperty("RowIndex")
    public void setRowIndex(String RowIndex) {
        this.RowIndex = RowIndex;
    }

}
