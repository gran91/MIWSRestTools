/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model.mi;

import com.kles.model.AbstractDataModel;

/**
 *
 * @author jchau
 */
public abstract class AbstractDataMIModel extends AbstractDataModel {
    protected String MIProgram="";
    protected String MITransaction="";
    
    public AbstractDataMIModel() {
    }

    public String getMIProgram() {
        return MIProgram;
    }

    public void setMIProgram(String MIProgram) {
        this.MIProgram = MIProgram;
    }

    public String getMITransaction() {
        return MITransaction;
    }

    public void setMITransaction(String MITransaction) {
        this.MITransaction = MITransaction;
    }
    
    
}
