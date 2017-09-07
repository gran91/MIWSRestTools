/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author jchau
 */
@JacksonXmlRootElement(localName = "Transaction")
public class Transaction {

    @JacksonXmlProperty(localName = "Program", isAttribute = true)
    private String Program;
    @JacksonXmlProperty(localName = "Transaction", isAttribute = true)
    private String Transaction;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String Description;
    @JacksonXmlProperty(localName = "Multi", isAttribute = true)
    private String Multi;
    @JacksonXmlProperty(localName = "InputFieldList")
    private InputFieldList InputFieldList;
    @JacksonXmlProperty(localName = "OutputFieldList")
    private OutputFieldList OutputFieldList;

//    @JsonProperty("Program")
    public String getProgram() {
        return Program;
    }

//    @JsonProperty("Program")
    public void setProgram(String program) {
        this.Program = program;
    }

//    @JsonProperty("Transaction")
    public String getTransaction() {
        return Transaction;
    }

//    @JsonProperty("Transaction")
    public void setTransaction(String transaction) {
        this.Transaction = transaction;
    }

//    @JsonProperty("Description")
    public String getDescription() {
        return Description;
    }

//    @JsonProperty("Description")
    public void setDescription(String description) {
        this.Description = description;
    }

//    @JsonProperty("Multi")
    public String getMulti() {
        return Multi;
    }

//    @JsonProperty("Multi")
    public void setMulti(String multi) {
        this.Multi = multi;
    }

//    @JsonProperty("InputFieldList")
    public InputFieldList getInput() {
        return InputFieldList;
    }

//    @JsonProperty("InputFieldList")
    public void setInput(InputFieldList input) {
        this.InputFieldList = input;
    }

//    @JsonProperty("OutputFieldList")
    public OutputFieldList getOutput() {
        return OutputFieldList;
    }

//    @JsonProperty("OutputFieldList")
    public void setOutput(OutputFieldList output) {
        this.OutputFieldList = output;
    }

    @Override
    public String toString() {
        return Transaction;
    }

}
