/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.mi;

import com.kles.model.IRestConnection;
import java.util.LinkedHashMap;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jchau
 */
@XmlRootElement(name = "MIInputData")
public class MIInputData {
    
    private IRestConnection restConnection;
    private Transaction transaction;
    private LinkedHashMap<String, String> data = new LinkedHashMap<>();

    @XmlTransient
    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public LinkedHashMap<String, String> getData() {
        return data;
    }

    public void setData(LinkedHashMap<String, String> data) {
        this.data = data;
    }
    
    
}
