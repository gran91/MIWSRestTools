/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.fx.custom.InputConstraints;
import com.kles.fx.custom.TextFieldValidator;
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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javax.ws.rs.core.MediaType;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MDBREADToolsController {

    @FXML
    private BorderPane root;

    @FXML
    private MIConnectionSimpleController miConnectionController;

    @FXML
    private MDBREADListManageController mdbreadListManageController;

    @FXML
    private MDBREADTransactionListManageController mdbreadTransactionListManageController;

    @FXML
    private TextField ttable, tindex;

    @FXML
    private Button bNew, bCreateGet, bCreateLst, bCreateSel;

    @FXML
    private ProgressIndicator progress;

    @FXML
    private Label lmessage;

    private IRestConnection restConnection;
    private Service<String> restServicePostClearCache;
    private RestGetTask restPostClearCache;
    private final DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private MainApp mainApp;
    private Stage stage;

    private final Map<BooleanBinding, String> messages = new LinkedHashMap<>();
    private BooleanBinding tableBoolean, indexBoolean;

    @FXML
    public void initialize() {
        tableBoolean = TextFieldValidator.emptyTextFieldBinding(ttable, "Table", messages);
        indexBoolean = TextFieldValidator.emptyTextFieldBinding(tindex, "Index table", messages);
        InputConstraints.lettersOnly(ttable, 6);
        InputConstraints.numbersOnly(tindex, 2);

//        mdbreadListManageController.getTableName().bind(ttable.textProperty());
//        mdbreadListManageController.getTableIndex().bind(tindex.textProperty());
        mdbreadTransactionListManageController.getListTransaction().addListener((ListChangeListener.Change<? extends Transaction> c) -> {
            mdbreadListManageController.getListTransaction().setAll(mdbreadTransactionListManageController.getListTransaction());
        });

        bNew.disableProperty().bind((tableBoolean.and(indexBoolean)));

        miConnectionController.getIsConnected().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                restConnection = miConnectionController.getRestConnection();
                restClient.setEnvironment(restConnection);
                mdbreadListManageController.setRestConnection(restConnection);
                mdbreadListManageController.getRestClient().setEnvironment(restConnection);
                mdbreadTransactionListManageController.setRestConnection(restConnection);
                mdbreadTransactionListManageController.getRestClient().setEnvironment(restConnection);
                runClearCache();
            } else {
                restConnection = null;
                mdbreadListManageController.setRestConnection(restConnection);
                mdbreadTransactionListManageController.setRestConnection(restConnection);
                mdbreadListManageController.getListInputTable().clear();
                mdbreadListManageController.getListInputTableFilter().clear();
                mdbreadListManageController.getListOutputTable().clear();
                mdbreadListManageController.getListOutputMI().clear();
                mdbreadTransactionListManageController.getListTransaction().clear();
            }
        });

        createRestServicePostClearCache();

        mdbreadListManageController.getRestServicePostClearCache().stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    mdbreadTransactionListManageController.runClearCache();
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    mdbreadTransactionListManageController.runClearCache();
                    break;
            }
        });

        progress.visibleProperty().bind(mdbreadListManageController.getRestServiceCreate().runningProperty()
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty()));

        lmessage.visibleProperty().bind(mdbreadListManageController.getRestServiceCreate().runningProperty()
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty()));

        bCreateGet.disableProperty().bind(Bindings.isEmpty(mdbreadListManageController.getListOutputMI())
                .or(mdbreadListManageController.getRestServiceCreate().runningProperty())
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty()));
        bCreateLst.disableProperty().bind(Bindings.isEmpty(mdbreadListManageController.getListOutputMI())
                .or(mdbreadListManageController.getRestServiceCreate().runningProperty())
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty())
                .or(mdbreadListManageController.getIsQueryMode()));
        bCreateSel.disableProperty().bind(Bindings.isEmpty(mdbreadListManageController.getListOutputMI())
                .or(mdbreadListManageController.getRestServiceCreate().runningProperty())
                .or(mdbreadListManageController.getRestServiceCreateFields().runningProperty())
                .or(mdbreadListManageController.getIsQueryMode()));

    }

    public void setTransactionData(Transaction t) {
        ttable.setText(t.getTransaction().substring(3, 9));
        tindex.setText(t.getTransaction().substring(9, 11));
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
                    mdbreadTransactionListManageController.runListTransaction();
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restPostClearCache.getMessage());
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    mdbreadTransactionListManageController.runListTransaction();
                    break;
            }
        });
    }

    @FXML
    private void createGet(ActionEvent event) {
        lmessage.setText("Create Get");
        mdbreadListManageController.runCreateTransaction("Get");
    }

    @FXML
    private void createLst(ActionEvent event) {
        lmessage.setText("Create Lst");
        mdbreadListManageController.runCreateTransaction("Lst");
    }

    @FXML
    private void createSel(ActionEvent event) {
        lmessage.setText("Create Sel");
        mdbreadListManageController.runCreateTransaction("Sel");
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

    @FXML
    private void getTableInfo(ActionEvent event) {
        mdbreadListManageController.getTableName().set(ttable.getText());
        mdbreadListManageController.getTableIndex().set(tindex.getText());
        mdbreadListManageController.runTableInfo();
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        miConnectionController.setMainApp(mainApp);
        mdbreadListManageController.setMainApp(mainApp);
        mdbreadTransactionListManageController.setMainApp(mainApp);
//        transactionList.setCellFactory((ListView<MIInputData> param) -> new MIInputDataListCell(mainApp));
        mdbreadTransactionListManageController.getTransactionList().setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && !mdbreadTransactionListManageController.getTransactionList().getSelectionModel().getSelectedIndices().isEmpty()) {
                final Transaction t = mdbreadTransactionListManageController.getTransactionList().getSelectionModel().getSelectedItem();
                setTransactionData(t);
            }
        });

    }

    public void setEnvironmentList(ObservableList list) {
        miConnectionController.setEnvironmentList(list);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        mdbreadTransactionListManageController.setStage(stage);
        mdbreadListManageController.setStage(stage);
    }
}
