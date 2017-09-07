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
public class MIResult {

    private String Program;
    private String Transaction;
    private MetaData Metadata;
    private List<MIRecord> MIRecord;


    @JsonProperty("Program")
    public String getProgram() {
        return Program;
    }

    @JsonProperty("Program")
    public void setProgram(String Program) {
        this.Program = Program;
    }

    @JsonProperty("Transaction")
    public String getTransaction() {
        return Transaction;
    }

    @JsonProperty("Transaction")
    public void setTransaction(String Transaction) {
        this.Transaction = Transaction;
    }

    @JsonProperty("Metadata")
    public MetaData getMetadata() {
        return Metadata;
    }

    @JsonProperty("Metadata")
    public void setMetadata(MetaData Metadata) {
        this.Metadata = Metadata;
    }

    @JsonProperty("MIRecord")
    public List<MIRecord> getMIRecord() {
        return MIRecord;
    }

    @JsonProperty("MIRecord")
    public void setMIRecord(List<MIRecord> MIRecord) {
        this.MIRecord = MIRecord;
    }

}
