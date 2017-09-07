/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

/**
 *
 * @author jchau
 */
public class MIPrograms {

    @JacksonXmlProperty(localName = "Name")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> name;
    @JacksonXmlProperty(isAttribute = true)
    private boolean ServiceAvailable;

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public boolean getServiceAvailable() {
        return ServiceAvailable;
    }

}
