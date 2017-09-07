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
@JacksonXmlRootElement(localName = "MIProgramMetadata")
public class MIProgramMetadata {
    @JacksonXmlProperty(isAttribute = true)
    private String Description;
    @JacksonXmlProperty(isAttribute = true)
    private String Program;
    @JacksonXmlProperty(localName = "Transaction")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Transaction> Transaction;
    
//    @JsonProperty("Description")
    public String getDescription() {
        return Description;
    }

//    @JsonProperty("Description")
    public void setDescription(String description) {
        this.Description = description;
    }

//    @JsonProperty("Program")
    public String getProgram() {
        return Program;
    }

//    @JsonProperty("Program")
    public void setProgram(String program) {
        this.Program = program;
    }

//    @JsonProperty("Transaction")
    public List<Transaction> getTransactions() {
        return Transaction;
    }

//    @JsonProperty("Transaction")
    public void setTransactions(List<Transaction> transactions) {
        this.Transaction = transactions;
    }
    
    
}
