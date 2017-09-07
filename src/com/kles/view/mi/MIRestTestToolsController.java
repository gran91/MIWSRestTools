/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.mi.MIInputData;
import com.kles.mi.MIResult;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.task.rest.RestGetTask;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIRestTestToolsController {

    @FXML
    private BorderPane root;

    @FXML
    private MIAPITransactionChooserController miAPITransactionController;

    @FXML
    private MIConnectionController miConnectionController;

    @FXML
    private MIRestTestController miRestTestController;

    @FXML
    private VBox left, center;

    @FXML
    private ListView<MIInputData> listSaveInput;

    private IRestConnection restConnection;
    private Service<String> restService;
    private RestGetTask restTask;
    private DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private Transaction transaction;
    private MIResult result;
    private MainApp mainApp;
    private Stage stage;

    @FXML
    public void initialize() {
        miConnectionController.getIsConnected().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                restConnection = miConnectionController.getRestConnection();
                miRestTestController.setRestConnection(restConnection);
                miAPITransactionController.setRestConnection(miConnectionController.getRestConnection());
                miAPITransactionController.setMIPrograms(miConnectionController.getMIPrograms());
                miAPITransactionController.getIsAPIDisable().set(false);
            } else {
                restConnection = null;
                miAPITransactionController.getComboAPI().setItems(FXCollections.observableArrayList());
                miAPITransactionController.getComboTrans().setItems(FXCollections.observableArrayList());
                miAPITransactionController.setRestConnection(null);
                miAPITransactionController.setMIPrograms(new ArrayList<>());
                miAPITransactionController.getIsAPIDisable().set(true);
                miAPITransactionController.getIsTransDisable().set(true);
            }
        });

        miAPITransactionController.getComboTrans().valueProperty().addListener((ObservableValue<? extends Transaction> observable, Transaction oldValue, Transaction newValue) -> {
            miRestTestController.setDataToInputPanel(newValue);
        });

        miAPITransactionController.getProgressAPI().visibleProperty().bind(miConnectionController.getRestService().runningProperty());
        listSaveInput.disableProperty().bind(miConnectionController.getIsConnected().not());
        miRestTestController.getMaxRecord().bind(miAPITransactionController.getMaxRecordValue());
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        miAPITransactionController.setMainApp(mainApp);
        miConnectionController.setMainApp(mainApp);
        miRestTestController.setMainApp(mainApp);
        miRestTestController.setStage(stage);
        listSaveInput.setItems(mainApp.getDataMap().get("MIInputData").getList());
        listSaveInput.setCellFactory((ListView<MIInputData> param) -> new MIInputDataListCell(mainApp));
        listSaveInput.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && !listSaveInput.getSelectionModel().getSelectedIndices().isEmpty()) {
                final ChangeListener changeService = new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                        if (newValue == Worker.State.SUCCEEDED) {
                            miAPITransactionController.getComboTrans().getSelectionModel().select(listSaveInput.getSelectionModel().getSelectedItem().getTransaction());
                            miRestTestController.buildInputPanelData(listSaveInput.getSelectionModel().getSelectedItem().getTransaction(), listSaveInput.getSelectionModel().getSelectedItem().getData());
                            miAPITransactionController.getRestService().stateProperty().removeListener(this);
                        }
                    }
                };
                miAPITransactionController.getRestService().stateProperty().addListener(changeService);
                if (miAPITransactionController.getComboAPI().getSelectionModel().getSelectedItem() != null) {
                    if (miAPITransactionController.getComboAPI().getSelectionModel().getSelectedItem().equals(listSaveInput.getSelectionModel().getSelectedItem().getTransaction().getProgram())) {

                        miRestTestController.buildInputPanelData(listSaveInput.getSelectionModel().getSelectedItem().getTransaction(), listSaveInput.getSelectionModel().getSelectedItem().getData());
                    }
                }
                miAPITransactionController.getComboAPI().getSelectionModel().select(listSaveInput.getSelectionModel().getSelectedItem().getTransaction().getProgram());
            }
        });
    }

    public void setEnvironmentList(ObservableList list) {
        miConnectionController.setEnvironmentList(list);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
