/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javax.ws.rs.core.MediaType;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MDBREADEditController {

    @FXML
    private BorderPane root;

    @FXML
    private Label ltransactionName;

    @FXML
    private MDBREADListManageController mdbreadListManageController;

    @FXML
    private Button bUpdate, bCreate1, bCreate2;

    @FXML
    private ProgressIndicator progress;

    @FXML
    private Label lmessage;

    private String transactionName = "";

    private IRestConnection restConnection;
    private Service<String> restServicePostClearCache;
    private RestGetTask restPostClearCache;
    private final DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private MainApp mainApp;
    private Stage stage;

    private String updateMethod, create1Method, create2Method;
    private final Map<BooleanBinding, String> messages = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        createRestServicePostClearCache();

        progress.visibleProperty().bind(mdbreadListManageController.getRestServiceCreate().runningProperty()
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty()));

        lmessage.visibleProperty().bind(mdbreadListManageController.getRestServiceCreate().runningProperty()
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty()));

        bUpdate.disableProperty().bind(Bindings.isEmpty(mdbreadListManageController.getListOutputMI())
                .or(mdbreadListManageController.getRestServiceCreate().runningProperty())
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty()));
        bCreate1.disableProperty().bind(Bindings.isEmpty(mdbreadListManageController.getListOutputMI())
                .or(mdbreadListManageController.getRestServiceCreate().runningProperty())
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty())
                .or(mdbreadListManageController.getIsQueryMode()));
        bCreate2.disableProperty().bind(Bindings.isEmpty(mdbreadListManageController.getListOutputMI())
                .or(mdbreadListManageController.getRestServiceCreate().runningProperty())
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty())
                .or(mdbreadListManageController.getIsQueryMode()));
    }

    public void setTransactionData(Transaction t) {
        ltransactionName.setText(t.getTransaction().trim());
        updateMethod = t.getTransaction().substring(0, 3);
        switch (updateMethod) {
            case "Get":
                create1Method = "Lst";
                bCreate1.setText("CreateLst");
                create2Method = "Sel";
                bCreate2.setText("CreateSel");
                break;
            case "Lst":
                create1Method = "Get";
                bCreate1.setText("CreateGet");
                create2Method = "Sel";
                bCreate2.setText("CreateSel");
                break;
            case "Sel":
                create1Method = "Get";
                bCreate1.setText("CreateGet");
                create2Method = "Lst";
                bCreate2.setText("CreateLst");
                break;
        }
        mdbreadListManageController.setTransactionData(t);
    }

    private void createRestServicePostClearCache() {
        restServicePostClearCache = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restPostClearCache;
            }
        };
        restServicePostClearCache.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restPostClearCache.getMessage());
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    break;
            }
        });
    }

    @FXML
    private void update(ActionEvent event) {
        lmessage.setText("Update " + transactionName);
        mdbreadListManageController.getUpdateMode().set(true);
        mdbreadListManageController.runCreateTransaction(updateMethod);
    }

    @FXML
    private void create1(ActionEvent event) {
        lmessage.setText("Create " + create1Method);
        mdbreadListManageController.getUpdateMode().set(false);
        mdbreadListManageController.runCreateTransaction(create1Method);
    }

    @FXML
    private void create2(ActionEvent event) {
        lmessage.setText("Create " + create2Method);
        mdbreadListManageController.getUpdateMode().set(false);
        mdbreadListManageController.runCreateTransaction(create2Method);
    }

    private void runClearCache() {
        restClient.setAction(DefaultRestClientMetadataConnection.CLEAR_CACHE);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MDBREADMI");
        restClient.setDataModel(m);
        restPostClearCache = new RestGetTask(restClient);
        restPostClearCache.setMethod(RestGetTask.POST);
        restPostClearCache.setMediaType(MediaType.APPLICATION_JSON);
        restServicePostClearCache.restart();
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        mdbreadListManageController.setMainApp(mainApp);
    }

    public void setTransaction(Transaction t) {
        setTransactionData(t);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public MDBREADListManageController getMdbreadListManageController() {
        return mdbreadListManageController;
    }

    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public DefaultRestClientMetadataConnection getRestClient() {
        return restClient;
    }

    public void setRestConnection(IRestConnection con) {
        restConnection = con;
        restClient.setEnvironment(restConnection);
        mdbreadListManageController.setRestConnection(restConnection);
        mdbreadListManageController.getRestClient().setEnvironment(restConnection);
    }

}
