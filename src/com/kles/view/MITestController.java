/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view;

import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.model.MIWS;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.view.util.ComboboxModelAdd;
import java.util.HashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javax.ws.rs.core.MediaType;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MITestController {

    @FXML
    private ComboboxModelAdd listEnv;
    @FXML
    private TextField programField;
    @FXML
    private TextField transactionField;

    private DefaultRestClientMetadataConnection metadataClient = new DefaultRestClientMetadataConnection();
    private final HashMap<String, Control> listFieldProgram = new HashMap<>();

    private MIWS environment;
    private BooleanProperty isEnvironment, isHeaderOK, isBodyOK, isRunning;
    private MainApp mainApp;

    @FXML
    private void initialize() {
        isEnvironment = new SimpleBooleanProperty(false);
        listFieldProgram.put("program", programField);
        programField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.F4) {
                promptProgram();
            }
        });

        transactionField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.F4) {
                promptTransaction();
            }
        });
        listEnv.getListModel().valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue != null) {
                isEnvironment.setValue(false);
                environment = (MIWS) listEnv.getListModel().getSelectionModel().getSelectedItem();
                programField.requestFocus();
            } else {
                isEnvironment.setValue(true);
            }
        });
    }

    public void promptProgram() {
        metadataClient = new DefaultRestClientMetadataConnection();
        metadataClient.setEnvironment((MIWS) listEnv.getListModel().getSelectionModel().getSelectedItem());
        PromptF4Util p = new PromptF4Util(metadataClient, PromptF4Util.API_PROGRAM, MediaType.APPLICATION_XML, listFieldProgram, programField, PromptF4Util.POPUP);
        p.autoPosition = true;
        p.show();
    }

    public void promptTransaction() {
        metadataClient = new DefaultRestClientMetadataConnection();
        metadataClient.setEnvironment((MIWS) listEnv.getListModel().getSelectionModel().getSelectedItem());
//        metadataClient.setProgram(programField.getText());
        PromptF4Util p = new PromptF4Util(metadataClient, PromptF4Util.API_TRANSACTION, MediaType.APPLICATION_XML, listFieldProgram, transactionField, PromptF4Util.POPUP);
        p.autoPosition = true;
        p.show();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        listEnv.setMainApp(mainApp);
        listEnv.setModel(new MIWS());
        if (!mainApp.getDataMap().containsKey("MIWS")) {
            ObservableList<MIWS> environmentData = FXCollections.observableArrayList();
            mainApp.addToDataMap("MIWS", environmentData);
        }
        listEnv.setList(mainApp.getDataMap().get("MIWS").getList());
        listEnv.init();
        FxUtil.autoCompleteComboBox(listEnv.getListModel(), FxUtil.AutoCompleteMode.STARTS_WITH);
    }
}
