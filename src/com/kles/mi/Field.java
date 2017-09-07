/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.kles.model.mi.GenericDataMIModel;
import org.apache.commons.lang.BooleanUtils;

/**
 *
 * @author jchau
 */
public class Field {

    @JacksonXmlProperty(isAttribute = true)
    private String Name;
    @JacksonXmlProperty(isAttribute = true)
    private String Description;
    @JacksonXmlProperty(isAttribute = true)
    private String Length;
    @JacksonXmlProperty(isAttribute = true)
    private String FieldType;
    @JacksonXmlProperty(isAttribute = true)
    private Boolean Mandatory;

    public Field() {
    }

//    @JsonProperty("Name")
    public String getName() {
        return Name;
    }

//    @JsonProperty("Name")
    public void setName(String name) {
        this.Name = name;
    }

//    @JsonProperty("Description")
    public String getDescription() {
        return Description;
    }

//    @JsonProperty("Description")
    public void setDescription(String description) {
        this.Description = description;
    }

//    @JsonProperty("FieldType")
    public String getFieldType() {
        return FieldType;
    }

//    @JsonProperty("FieldType")
    public void setFieldType(String type) {
        this.FieldType = type;
    }

//    @JsonProperty("Length")
    public String getLength() {
        return Length;
    }

//    @JsonProperty("Length")
    public void setLength(String length) {
        this.Length = length;
    }

//    @JsonProperty("Mandatory")
    public Boolean getMandatory() {
        return Mandatory;
    }

//    @JsonProperty("Mandatory")
    public void setMandatory(Boolean mandatory) {
        this.Mandatory = mandatory;
    }

    public GenericDataMIModel buildFieldModel(GenericDataMIModel m, String putFieldType, int frpo) {
        m.addData("TRTP", putFieldType);
        m.addData("FLNM", Name);
        m.addData("FLDS", Description);
        m.addData("FRPO", "" + frpo);
        m.addData("LENG", "" + Length);
        m.addData("TYPE", "" + FieldType);
        m.addData("MAND", BooleanUtils.toInteger(Mandatory) + "");
        return m;
    }
}
