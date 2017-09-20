/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.fx.custom.InputConstraints;
import com.kles.mi.MIProgramMetadata;
import com.kles.mi.MIPrograms;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import com.kles.utils.MIUtils;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javax.ws.rs.core.MediaType;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIAPITransactionChooserController {

    @FXML
    private Label lAPIName;

    @FXML
    private ComboBox<String> comboAPI;

    @FXML
    private Label lTransactionName;

    @FXML
    private ComboBox<Transaction> comboTrans;

    @FXML
    private TextField tmaxrec;

    @FXML
    private ProgressIndicator progressAPI;

    @FXML
    private ProgressIndicator progressTransaction;

    private BooleanProperty isAPIDisable, isTransDisable;

    private final ObservableList<String> listAPI = FXCollections.observableArrayList();
    private final ObservableList<Transaction> listTransaction = FXCollections.observableArrayList();
    private IRestConnection restConnection;
    private Service<String> restService;
    private RestGetTask restTask;
    private DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private MIProgramMetadata miprogram;
    private MainApp mainApp;
    private IntegerProperty maxRecordValue = new SimpleIntegerProperty(0);

    @FXML
    public void initialize() {
        InputConstraints.decimalOnly(tmaxrec, 0);
        isAPIDisable = new SimpleBooleanProperty(true);
        isTransDisable = new SimpleBooleanProperty(true);
        comboAPI.setItems(listAPI);
        comboTrans.setItems(listTransaction);
        FxUtil.autoCompleteComboBox(comboAPI, FxUtil.AutoCompleteMode.STARTS_WITH);
        FxUtil.autoCompleteComboBox(comboTrans, FxUtil.AutoCompleteMode.STARTS_WITH);
        comboAPI.disableProperty().bind(isAPIDisable);
        comboTrans.disableProperty().bind(isTransDisable);
        tmaxrec.disableProperty().bind(isAPIDisable);
        tmaxrec.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (newValue != null) {
                        maxRecordValue.set(Integer.parseInt(newValue.trim()));
                    }
                } catch (NumberFormatException e) {
                    maxRecordValue.set(0);
                }
            }
        }
        );

        createRestServiceProvider();

        progressTransaction.visibleProperty()
                .bind(restService.runningProperty());

        comboAPI.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    comboTrans.getSelectionModel().select(null);
                    if (!newValue.isEmpty() && restConnection != null) {
                        restClient.setEnvironment(restConnection);
                        restClient.setAction(DefaultRestClientMetadataConnection.LIST_TRANS);
                        GenericDataMIModel m = new GenericDataMIModel();
                        m.setMIProgram(newValue);
                        restClient.setDataModel(m);
                        restTask = new RestGetTask(restClient);
                        restTask.setMediaType(MediaType.APPLICATION_XML);
                        restService.restart();
                    } else {
                        lAPIName.setText("");
                        listTransaction.clear();
                        isTransDisable.set(true);
                    }
                } else {
                    lAPIName.setText("");
                    listTransaction.clear();
                    isTransDisable.set(true);
                }
            }
        }
        );

        comboTrans.valueProperty().addListener(new ChangeListener<Transaction>() {
            @Override
            public void changed(ObservableValue<? extends Transaction> observable, Transaction oldValue,
                    Transaction newValue
            ) {
                if (newValue != null) {
                    lTransactionName.setText(newValue.getDescription());
                } else {
                    lTransactionName.setText("");
                }
            }
        }
        );
    }

    private void createRestServiceProvider() {
        restService = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTask;
            }
        };
        restService.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    cleanAPITrans();
                    break;
                case FAILED:
                    cleanAPITrans();
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTask.getMessage());
                    break;
                case CANCELLED:
                    cleanAPITrans();
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    if (!restTask.getValue().isEmpty()) {
                        ObjectMapper mapper = new XmlMapper();
                        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                        try {
                            miprogram = mapper.readValue(restTask.getValue(), MIProgramMetadata.class);
                            List<Transaction> listTrans = miprogram.getTransactions();
                            listTrans = MIUtils.purify(listTrans);
                            listTransaction.setAll(listTrans);
                            Platform.runLater(() -> {
                                lAPIName.setText(miprogram.getDescription());
                            });
                            isTransDisable.set(false);
                        } catch (Exception e) {
                            cleanAPITrans();
                            FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), e.getMessage());
                        }
                    }
                    break;
            }
        }
        );
    }

    private void cleanAPITrans() {
        lAPIName.setText("");
        isTransDisable.set(true);
        listTransaction.clear();
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public ComboBox<String> getComboAPI() {
        return comboAPI;
    }

    public ComboBox<Transaction> getComboTrans() {
        return comboTrans;
    }

    public void setMIPrograms(MIPrograms mi) {
        setMIPrograms(mi.getName());
    }

    public void setMIPrograms(List<String> list) {
        //comboAPI.setItems(FXCollections.observableList(list));
        listAPI.setAll(list);
    }

    public MIProgramMetadata getMIProgramMetadata() {
        return miprogram;
    }

    public void setMIProgramMetadata(MIProgramMetadata miprogram) {
        this.miprogram = miprogram;
    }

    public BooleanProperty getIsAPIDisable() {
        return isAPIDisable;
    }

    public void setIsAPIDisable(BooleanProperty isAPIDisable) {
        this.isAPIDisable = isAPIDisable;
    }

    public BooleanProperty getIsTransDisable() {
        return isTransDisable;
    }

    public void setIsTransDisable(BooleanProperty isTransDisable) {
        this.isTransDisable = isTransDisable;
    }

    public ProgressIndicator getProgressAPI() {
        return progressAPI;
    }

    public void setProgressAPI(ProgressIndicator progressAPI) {
        this.progressAPI = progressAPI;
    }

    public ProgressIndicator getProgressTransaction() {
        return progressTransaction;
    }

    public void setProgressTransaction(ProgressIndicator progressTransaction) {
        this.progressTransaction = progressTransaction;
    }

    public TextField getTmaxrec() {
        return tmaxrec;
    }

    public void setTmaxrec(TextField tmaxrec) {
        this.tmaxrec = tmaxrec;
    }

    public Service<String> getRestService() {
        return restService;
    }

    public void setRestService(Service<String> restService) {
        this.restService = restService;
    }

    public RestGetTask getRestTask() {
        return restTask;
    }

    public void setRestTask(RestGetTask restTask) {
        this.restTask = restTask;
    }

    public DefaultRestClientMetadataConnection getRestClient() {
        return restClient;
    }

    public void setRestClient(DefaultRestClientMetadataConnection restClient) {
        this.restClient = restClient;
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
    }

    public int getMaxRecord() {
        int n = 0;
        if (!tmaxrec.getText().isEmpty()) {
            try {
                n = Integer.parseInt(tmaxrec.getText().trim());
            } catch (NumberFormatException e) {
            }
        }
        return n;
    }

    public IntegerProperty getMaxRecordValue() {
        return maxRecordValue;
    }
}
