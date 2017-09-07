/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author jchau
 */
public class MetaData {

    private List<FieldMetadata> Field;

    @JsonProperty("Field")
    public List<FieldMetadata> getField() {
        return Field;
    }

    @JsonProperty("Field")
    public void setField(List<FieldMetadata> Field) {
        this.Field = Field;
    }

}
