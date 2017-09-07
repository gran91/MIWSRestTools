/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

/**
 *
 * @author jchau
 */
@JacksonXmlRootElement(localName = "InputFieldList")
public class InputFieldList {

    @JacksonXmlProperty(localName = "Field")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Field> Field;

    public InputFieldList() {
    }

//        @JsonProperty("Field")
    public List<Field> getField() {
        return Field;
    }

//        @JsonProperty("Field")
    public void setField(List<Field> field) {
        this.Field = field;
    }

}
