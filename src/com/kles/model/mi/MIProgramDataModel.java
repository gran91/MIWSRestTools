/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model.mi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.MIError;
import com.kles.mi.MIProgramMetadata;
import com.kles.model.MIWS;
import com.kles.task.rest.RestGetTask;
import java.io.IOException;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;

/**
 *
 * @author jchau
 */
public class MIProgramDataModel {

    private String program = "";
    private MIWS env;
    private MIProgramMetadata metadata;
    private RestGetTask task;

    public MIProgramDataModel(String prog) {
        program = prog;
    }

    public void init() {
        DefaultRestClientMetadataConnection client = new DefaultRestClientMetadataConnection();
        client.setEnvironment(env);
        client.getDataModel().setMIProgram(program);
        client.getWebTarget();
        task = new RestGetTask(client);
        task.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                if (!task.getValue().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        metadata = mapper.readValue(task.getValue(), MIProgramMetadata.class);
                    } catch (Exception e) {
                        try {
                            MIError err = mapper.readValue(task.getValue(), MIError.class);
                            FxUtil.showAlert(Alert.AlertType.ERROR, MainApp.resourceMessage.getString("error.API"), err.getType(), err.getMessage());
                        } catch (IOException ex) {
                            FxUtil.showAlert(Alert.AlertType.ERROR, MainApp.resourceMessage.getString("error.API"), MainApp.resourceMessage.getString("error.unknown"), MainApp.resourceMessage.getString("error.unknown"));
                        }
                    }
                }
            }
        });
    }

    public void execute() {
        task.run();
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public MIWS getEnv() {
        return env;
    }

    public void setEnv(MIWS env) {
        this.env = env;
    }

    public MIProgramMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MIProgramMetadata metadata) {
        this.metadata = metadata;
    }

    public RestGetTask getTask() {
        return task;
    }

    public void setTask(RestGetTask task) {
        this.task = task;
    }
}
