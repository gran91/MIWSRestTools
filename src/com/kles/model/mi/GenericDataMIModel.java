/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model.mi;

import com.kles.model.AbstractDataModel;
import java.util.LinkedHashMap;

/**
 *
 * @author jchau
 */
public class GenericDataMIModel extends AbstractDataMIModel{

    private LinkedHashMap<String,String> inputData=new LinkedHashMap<>();

    public GenericDataMIModel() {
    }
    
    public void addData(String key,String value){
        inputData.put(key, value);
    }

    public LinkedHashMap<String, String> getInputData() {
        return inputData;
    }

    public void setInputData(LinkedHashMap<String, String> inputData) {
        this.inputData = inputData;
    }
    
    @Override
    public AbstractDataModel newInstance() {
        return new GenericDataMIModel();
    }
    
}
