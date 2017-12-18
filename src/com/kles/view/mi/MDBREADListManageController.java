/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.Field;
import com.kles.mi.MIProgramMetadata;
import com.kles.mi.MIRecord;
import com.kles.mi.MIResult;
import com.kles.mi.NameValue;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import com.kles.utils.MIUtils;
import static com.kles.utils.MIUtils.addFieldFromMIRecord;
import static com.kles.utils.MIUtils.calculateFRPO;
import static com.kles.utils.MIUtils.fieldToMIRecord;
import static com.kles.utils.MIUtils.getDescriptionTransactionFromKeys;
import static com.kles.utils.MIUtils.getTransactionTypeFromMethod;
import static com.kles.utils.MIUtils.removeMIRecordFromField;
import com.kles.view.util.PanelIndicator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.ws.rs.core.MediaType;
import org.controlsfx.control.textfield.TextFields;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MDBREADListManageController {

    @FXML
    private GridPane root;

    @FXML
    private TextField tfiltertable;

    @FXML
    private Label linputindex, ltablefield, loutpurfield;

    @FXML
    private ListView<MIRecord> lInputTable, lOutputTable, lOutputMI;
    private PanelIndicator progressInputTable, progressOutputTable, progressOutputMI;

    @FXML
    private Button bAdd, bRemove, bRestoreField, bRemoveLastField, bSearchQuery;

    @FXML
    private HBox searchBar;

    private String transactionName = "";
    private final ObservableList<MIRecord> listInputTable = FXCollections.observableArrayList();
    private final ObservableList<MIRecord> listInputTableFilter = FXCollections.observableArrayList();
    private final ObservableList<MIRecord> listOutputTable = FXCollections.observableArrayList();
    private final ObservableList<MIRecord> listOutputMI = FXCollections.observableArrayList();
    private FilteredList<MIRecord> filteredOutField;

    private IRestConnection restConnection;
    private Service<String> restServiceDeleteTransaction, restServiceGetInfoTableIN, restServiceGetInfoTableOUT, restServiceCreate, restServiceCreateFields, restServicePostClearCache;

    private RestGetTask restTask, restTaskDelTrans, restTaskIN, restTaskOUT, restTaskCreate, restTaskCreateFields, restPostClearCache;
    private final DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private Transaction transaction;
    private MIResult result;
    private MIProgramMetadata miprogram;
    private MainApp mainApp;
    private Stage stage;
    private IntegerProperty cptFields = new SimpleIntegerProperty(0);
    private BooleanProperty isQueryMode = new SimpleBooleanProperty(false);
    private StringProperty tableName = new SimpleStringProperty("");
    private StringProperty tableIndex = new SimpleStringProperty("");
    private StringProperty suffix = new SimpleStringProperty("");
    private BooleanProperty updateMode = new SimpleBooleanProperty(false);
    private final ObservableList<Transaction> listTransaction = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        createRestServiceDeleteTransaction();
        createRestServicePostClearCache();
        createRestServiceGetInfoTableIN();
        createRestServiceGetInfoTableOUT();
        createRestServiceCreate();
        createRestServiceCreateFields();

        tfiltertable = TextFields.createClearableTextField();
        HBox.setHgrow(tfiltertable, Priority.SOMETIMES);
        filteredOutField = new FilteredList(listOutputTable, s -> true);
        tfiltertable.textProperty().addListener(obs -> {
            String filter = tfiltertable.getText();
            if (filter == null || filter.length() == 0) {
                filteredOutField.setPredicate(s -> true);
            } else {
                filteredOutField.setPredicate(s -> (s.getNameValue().get(0).getValue().contains(filter)
                        || s.getNameValue().get(0).getValue().substring(2).contains(filter)
                        || s.getNameValue().get(4).getValue().contains(filter)));
            }
        });
        searchBar.getChildren().set(1, tfiltertable);

        progressInputTable = PanelIndicator.create().build().setService(restServiceGetInfoTableIN);
        progressOutputTable = PanelIndicator.create().build().setService(restServiceGetInfoTableOUT);
        progressInputTable.getPanel().visibleProperty().bind(restServiceGetInfoTableIN.runningProperty());
        progressOutputTable.getPanel().visibleProperty().bind(restServiceGetInfoTableOUT.runningProperty());

        root.add(progressInputTable.getPanel(), 0, 1);
        root.add(progressOutputTable.getPanel(), 1, 1);

        lInputTable.setItems(listInputTableFilter);
        lOutputTable.setItems(filteredOutField);
        lOutputMI.setItems(listOutputMI);
        lOutputTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lOutputMI.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lInputTable.setCellFactory((ListView<MIRecord> param) -> new MIRecordListCell(mainApp));
        lOutputTable.setCellFactory((ListView<MIRecord> param) -> new MIRecordListCell(mainApp));
        lOutputMI.setCellFactory(new Callback<ListView<MIRecord>, ListCell<MIRecord>>() {
            @Override
            public ListCell<MIRecord> call(ListView<MIRecord> param) {
                final MIRecordListCell MIListCell = new MIRecordListCell(mainApp);
                MIListCell.setDraggable(true);
                MIListCell.setListData(listOutputMI);
                return MIListCell;
            }
        });

//        bRestoreField.disableProperty().bind((Bindings.size(listInputTable).isEqualTo(Bindings.size(listInputTableFilter))).or(isQueryMode.not()));
        bRestoreField.disableProperty().bind(Bindings.isEmpty(listInputTable));
        bRemoveLastField.disableProperty().bind(Bindings.isEmpty(listInputTable).or(isQueryMode));
        bSearchQuery.disableProperty().bind(Bindings.isEmpty(listInputTable).or(isQueryMode));

        tfiltertable.disableProperty().bind(Bindings.isEmpty(listOutputTable));
        bAdd.disableProperty().bind(lOutputTable.getSelectionModel().selectedItemProperty().isNull());

        bRemove.disableProperty().bind(lOutputMI.getSelectionModel().selectedItemProperty().isNull());
    }

    public void setTransactionData(Transaction t) {
        tableName.set(t.getTransaction().substring(3, 9));
        tableIndex.set(t.getTransaction().substring(9, 11));
        if (t.getTransaction().length() > 11) {
            suffix.set(t.getTransaction().substring(11));
        } else {
            suffix.set("");
        }
        transaction = t;
        final ChangeListener changeService = new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {

                    removeMIRecordFromField(listInputTableFilter, transaction.getInput().getField());

                    if (transaction.getOutput() != null) {
                        removeMIRecordFromField(listOutputMI, transaction.getOutput().getField());

                        listOutputMI.clear();
                        transaction.getOutput().getField().forEach((Field t1) -> {
                            listOutputMI.add(fieldToMIRecord(listOutputTable, t1));
                        });
                    }
                    restServiceGetInfoTableOUT.stateProperty().removeListener(this);
                }
            }
        };
        restServiceGetInfoTableOUT.stateProperty().addListener(changeService);
        runTableInfo();
    }

    private void runClearCache() {
        suffix.set("");
        restClient.setAction(DefaultRestClientMetadataConnection.CLEAR_CACHE);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MDBREADMI");
        restClient.setDataModel(m);
        restPostClearCache = new RestGetTask(restClient);
        restPostClearCache.setMethod(RestGetTask.POST);
        restPostClearCache.setMediaType(MediaType.APPLICATION_JSON);
        restServicePostClearCache.restart();
    }

    public static List<MIRecord> suppressFirstCONO(List<MIRecord> list) {
        MIRecord r = list.get(0);
        if (r.getNameValue().get(0).getValue().trim().substring(2).equals("CONO")) {
            list.remove(r);
        }
        return list;
    }

    private void createRestServiceDeleteTransaction() {
        restServiceDeleteTransaction = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskDelTrans;
            }
        };
        restServiceDeleteTransaction.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restServiceDeleteTransaction.getException().getMessage(), (Exception) restServiceDeleteTransaction.getException());
                    runClearCache();
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    if (updateMode.get()) {
                        updateMode.set(false);
                        runAddTransaction();
                    } else {
                        runClearCache();
                    }
                    break;
            }
        }
        );
    }

    private void createRestServiceGetInfoTableIN() {
        restServiceGetInfoTableIN = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskIN;
            }
        };
        restServiceGetInfoTableIN.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restServiceGetInfoTableIN.getException().getMessage(), (Exception) restServiceGetInfoTableIN.getException());
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    if (!restTaskIN.getValue().isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            MIResult res = mapper.readValue(restTaskIN.getValue(), MIResult.class);
                            listInputTable.setAll(suppressFirstCONO(res.getMIRecord()));
                            listInputTableFilter.setAll(suppressFirstCONO(res.getMIRecord()));
                            GenericDataMIModel mOUT = new GenericDataMIModel();
                            mOUT.setMIProgram("MRS001MI");
                            mOUT.setMITransaction("LstFieldInfo");
                            LinkedHashMap<String, String> listOut = new LinkedHashMap();
                            listOut.put("FILE", tableName.get());
                            mOUT.setInputData(listOut);
                            restClient.setDataModel(mOUT);
                            restTaskOUT = new RestGetTask(restClient);
                            restServiceGetInfoTableOUT.restart();
                        } catch (Exception e) {
                            listInputTable.clear();
                            listInputTableFilter.clear();
                            listOutputTable.clear();
                            listOutputMI.clear();
                            try {
                                MIUtils.showMIError(restTaskIN.getValue());
                            } catch (IOException ex) {
                                FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), ex.getMessage(), (Exception) ex);
                            }
                        }
                    }
                    break;
            }
        }
        );
    }

    private void createRestServiceGetInfoTableOUT() {
        restServiceGetInfoTableOUT = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskOUT;
            }
        };
        restServiceGetInfoTableOUT.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTaskOUT.getMessage());
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    if (!restTaskOUT.getValue().isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            MIResult res = mapper.readValue(restTaskOUT.getValue(), MIResult.class);
                            listOutputTable.setAll(res.getMIRecord());
                        } catch (Exception e) {
                            FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), e.getMessage());
                        }
                    }
                    break;
            }
        }
        );
    }

    private void createRestServiceCreate() {
        restServiceCreate = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskCreate;
            }
        };
        restServiceCreate.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTaskCreate.getException().getMessage(), (Exception) restTaskCreate.getException());
//                    runClearCache();
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    cptFields.set(0);
                    runCreateFields(transactionName);
                    break;
            }
        }
        );
    }

    private void createRestServiceCreateFields() {
        restServiceCreateFields = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskCreateFields;
            }
        };
        restServiceCreateFields.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTaskCreateFields.getException().getMessage(), (Exception) restTaskCreateFields.getException());
//                    runClearCache();
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    runCreateFields(transactionName);
                    cptFields.set(cptFields.get() + 1);
                    break;
            }
        }
        );
    }

    @FXML
    private void createGet(ActionEvent event) {
        runCreateTransaction("Get");
    }

    @FXML
    private void createLst(ActionEvent event) {
        runCreateTransaction("Lst");
    }

    @FXML
    private void createSel(ActionEvent event) {
        runCreateTransaction("Sel");
    }

    public static boolean isTransactionInList(List<Transaction> list, String name) {
        for (Transaction t : list) {
            if (t.getTransaction().trim().equals(name.trim())) {
                return true;
            }
        }
        return false;
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
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restPostClearCache.getMessage(), (Exception) restPostClearCache.getException());
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

    public void runCreateTransaction(String typeMethod) {
        boolean testOK = false;
        transactionName = typeMethod + tableName.get() + tableIndex.get() + suffix.get();
        ButtonType bUpdate = new ButtonType(mainApp.getResourceBundle().getString("mi.update"), ButtonBar.ButtonData.OK_DONE);
        ButtonType bSuffix = new ButtonType(mainApp.getResourceBundle().getString("mi.suffix"), ButtonBar.ButtonData.YES);
        ButtonType bCancel = new ButtonType(mainApp.getResourceBundle().getString("main.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(AlertType.WARNING, mainApp.getResourceBundle().getString("mi.transaction.suffix.confirm"), bUpdate, bSuffix, bCancel);
        alert.setTitle(transactionName);
        Optional<ButtonType> resultDialog;
        if (!updateMode.get() && isTransactionInList(listTransaction, transactionName)) {
            resultDialog = alert.showAndWait();
            if (resultDialog.isPresent()) {
                if (resultDialog.get() == bUpdate) {
                    testOK = true;
                    updateMode.set(true);
                } else if (resultDialog.get() == bSuffix) {
                    testOK = true;
                    updateMode.set(false);
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("MDBREADMI");
                    dialog.setHeaderText(String.format(mainApp.getResourceBundle().getString("mi.transaction.already.exist"), transactionName));
                    dialog.setContentText(mainApp.getResourceBundle().getString("mi.transaction.suffix.new") + ":");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        suffix.set(result.get());
                        runCreateTransaction(typeMethod);
                    }
                } else {
                    return;
                }
            }
        } else {
            testOK = true;
        }
        if (testOK) {
            if (updateMode.get()) {
                runDeleteTransaction(transactionName);
            } else {
                runAddTransaction();
            }
        }
    }

    public void runAddTransaction() {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("AddTransaction");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", transactionName);
        m.addData("TRDS", getDescriptionTransactionFromKeys(listInputTableFilter));
        m.addData("STAT", "20");
        m.addData("SIMU", getTransactionTypeFromMethod(transactionName.substring(0, 3)));
        restClient.setDataModel(m);
        restTaskCreate = new RestGetTask(restClient);
        restTaskCreate.setMediaType(MediaType.APPLICATION_JSON);
        restServiceCreate.restart();
    }

    private void runDeleteTransaction(String t) {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("DelTransaction");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", t);
        restClient.setDataModel(m);
        restTaskDelTrans = new RestGetTask(restClient);
        restTaskDelTrans.setMediaType(MediaType.APPLICATION_JSON);
        restServiceDeleteTransaction.restart();
    }

    public void runCreateFields(String transactionName) {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("AddField");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", transactionName);
        if (cptFields.get() < listInputTableFilter.size()) {
            if (transactionName.startsWith("Get") || cptFields.get() != listInputTableFilter.size() - 1) {
                addFieldFromMIRecord(m, listInputTableFilter.get(cptFields.get()), "I", calculateFRPO(listInputTableFilter, cptFields.get()), "1");
            } else {
                addFieldFromMIRecord(m, listInputTableFilter.get(cptFields.get()), "I", calculateFRPO(listInputTableFilter, cptFields.get()));
            }
        } else if (cptFields.get() < listInputTableFilter.size() + listOutputMI.size()) {
            addFieldFromMIRecord(m, listOutputMI.get(cptFields.get() - listInputTableFilter.size()), "O", calculateFRPO(listOutputMI, cptFields.get() - listInputTableFilter.size()));
        } else {
            runClearCache();
            return;
        }

        restClient.setDataModel(m);
        restTaskCreateFields = new RestGetTask(restClient);
        restTaskCreateFields.setMediaType(MediaType.APPLICATION_JSON);
        restServiceCreateFields.restart();
    }

    @FXML
    private void searchQuery(ActionEvent event) {
        isQueryMode.set(true);
        listInputTableFilter.clear();
        List<MIRecord> listR = new ArrayList();
        MIRecord r = new MIRecord();
        List<NameValue> list = new ArrayList<>();
        list.add(new NameValue("FLNM", "SRQY"));
        list.add(new NameValue("TYPE", "A"));
        list.add(new NameValue("LENG", "120"));
        list.add(new NameValue("DECI", "0"));
        list.add(new NameValue("FLDS", "SearchQuery"));
        r.setNameValue(list);
        listR.add(r);
        listInputTableFilter.setAll(listR);
    }

    @FXML
    private void export(ActionEvent event) {

    }

    @FXML
    private void getTableInfo(ActionEvent event) {
        runTableInfo();
    }

    public void runTableInfo() {
        isQueryMode.set(false);
        listOutputMI.clear();
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel mIN = new GenericDataMIModel();
        mIN.setMIProgram("MRS001MI");
        mIN.setMITransaction("LstLFFields");
        LinkedHashMap<String, String> listIn = new LinkedHashMap();
        listIn.put("FILE", tableName.get());
        listIn.put("INDX", tableIndex.get());
        mIN.setInputData(listIn);
        restClient.setDataModel(mIN);
        restTaskIN = new RestGetTask(restClient);
        restServiceGetInfoTableIN.restart();
    }

    @FXML
    private void removeLastField(ActionEvent event) {
        listInputTableFilter.remove(listInputTableFilter.size() - 1);
    }

    @FXML
    private void restoreField(ActionEvent event) {
        listInputTableFilter.setAll(listInputTable);
        isQueryMode.set(false);
    }

    @FXML
    private void addField(ActionEvent event) {
        lOutputTable.getSelectionModel().getSelectedItems().forEach(new Consumer<MIRecord>() {
            @Override
            public void accept(MIRecord t) {
                boolean test = true;
                for (MIRecord m2 : listOutputMI) {
                    if (t.getNameValue().get(0).getValue().trim().equals(m2.getNameValue().get(0).getValue().trim())) {
                        test = false;
                        break;
                    }
                }
                if (test) {
                    listOutputMI.add(t);
                }
            }
        });
    }

    @FXML
    private void removeField(ActionEvent event) {
        lOutputMI.getSelectionModel().getSelectedItems().forEach(new Consumer<MIRecord>() {
            @Override
            public void accept(MIRecord t) {
                listOutputMI.remove(t);
            }
        });
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        linputindex.textProperty().bind(Bindings.concat(mainApp.getResourceBundle().getString("mi.inputindex")).concat(" ").concat(tableName).concat(tableIndex));
        ltablefield.textProperty().bind(Bindings.concat(mainApp.getResourceBundle().getString("mi.tablefield")).concat(" ").concat(tableName));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public GridPane getRoot() {
        return root;
    }

    public void setRoot(GridPane root) {
        this.root = root;
    }

    public TextField getTfiltertable() {
        return tfiltertable;
    }

    public void setTfiltertable(TextField tfiltertable) {
        this.tfiltertable = tfiltertable;
    }

    public ListView<MIRecord> getlInputTable() {
        return lInputTable;
    }

    public void setlInputTable(ListView<MIRecord> lInputTable) {
        this.lInputTable = lInputTable;
    }

    public ListView<MIRecord> getlOutputTable() {
        return lOutputTable;
    }

    public void setlOutputTable(ListView<MIRecord> lOutputTable) {
        this.lOutputTable = lOutputTable;
    }

    public ListView<MIRecord> getlOutputMI() {
        return lOutputMI;
    }

    public void setlOutputMI(ListView<MIRecord> lOutputMI) {
        this.lOutputMI = lOutputMI;
    }

    public PanelIndicator getProgressInputTable() {
        return progressInputTable;
    }

    public void setProgressInputTable(PanelIndicator progressInputTable) {
        this.progressInputTable = progressInputTable;
    }

    public PanelIndicator getProgressOutputTable() {
        return progressOutputTable;
    }

    public void setProgressOutputTable(PanelIndicator progressOutputTable) {
        this.progressOutputTable = progressOutputTable;
    }

    public PanelIndicator getProgressOutputMI() {
        return progressOutputMI;
    }

    public void setProgressOutputMI(PanelIndicator progressOutputMI) {
        this.progressOutputMI = progressOutputMI;
    }

    public Button getbAdd() {
        return bAdd;
    }

    public void setbAdd(Button bAdd) {
        this.bAdd = bAdd;
    }

    public Button getbRemove() {
        return bRemove;
    }

    public void setbRemove(Button bRemove) {
        this.bRemove = bRemove;
    }

    public Button getbRestoreField() {
        return bRestoreField;
    }

    public void setbRestoreField(Button bRestoreField) {
        this.bRestoreField = bRestoreField;
    }

    public Button getbRemoveLastField() {
        return bRemoveLastField;
    }

    public void setbRemoveLastField(Button bRemoveLastField) {
        this.bRemoveLastField = bRemoveLastField;
    }

    public Button getbSearchQuery() {
        return bSearchQuery;
    }

    public void setbSearchQuery(Button bSearchQuery) {
        this.bSearchQuery = bSearchQuery;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public FilteredList<MIRecord> getFilteredOutField() {
        return filteredOutField;
    }

    public void setFilteredOutField(FilteredList<MIRecord> filteredOutField) {
        this.filteredOutField = filteredOutField;
    }

    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public Service<String> getRestServiceGetInfoTableIN() {
        return restServiceGetInfoTableIN;
    }

    public void setRestServiceGetInfoTableIN(Service<String> restServiceGetInfoTableIN) {
        this.restServiceGetInfoTableIN = restServiceGetInfoTableIN;
    }

    public Service<String> getRestServiceGetInfoTableOUT() {
        return restServiceGetInfoTableOUT;
    }

    public void setRestServiceGetInfoTableOUT(Service<String> restServiceGetInfoTableOUT) {
        this.restServiceGetInfoTableOUT = restServiceGetInfoTableOUT;
    }

    public Service<String> getRestServiceCreate() {
        return restServiceCreate;
    }

    public void setRestServiceCreate(Service<String> restServiceCreate) {
        this.restServiceCreate = restServiceCreate;
    }

    public Service<String> getRestServiceCreateFields() {
        return restServiceCreateFields;
    }

    public void setRestServiceCreateFields(Service<String> restServiceCreateFields) {
        this.restServiceCreateFields = restServiceCreateFields;
    }

    public RestGetTask getRestTask() {
        return restTask;
    }

    public void setRestTask(RestGetTask restTask) {
        this.restTask = restTask;
    }

    public RestGetTask getRestTaskIN() {
        return restTaskIN;
    }

    public void setRestTaskIN(RestGetTask restTaskIN) {
        this.restTaskIN = restTaskIN;
    }

    public RestGetTask getRestTaskOUT() {
        return restTaskOUT;
    }

    public void setRestTaskOUT(RestGetTask restTaskOUT) {
        this.restTaskOUT = restTaskOUT;
    }

    public RestGetTask getRestTaskCreate() {
        return restTaskCreate;
    }

    public void setRestTaskCreate(RestGetTask restTaskCreate) {
        this.restTaskCreate = restTaskCreate;
    }

    public RestGetTask getRestTaskCreateFields() {
        return restTaskCreateFields;
    }

    public void setRestTaskCreateFields(RestGetTask restTaskCreateFields) {
        this.restTaskCreateFields = restTaskCreateFields;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public MIResult getResult() {
        return result;
    }

    public void setResult(MIResult result) {
        this.result = result;
    }

    public MIProgramMetadata getMiprogram() {
        return miprogram;
    }

    public void setMiprogram(MIProgramMetadata miprogram) {
        this.miprogram = miprogram;
    }

    public IntegerProperty getCptFieldsProperty() {
        return cptFields;
    }

    public int getCptFields() {
        return cptFields.get();
    }

    public void setCptFields(int cptFields) {
        this.cptFields.set(cptFields);
    }

    public BooleanProperty getIsQueryMode() {
        return isQueryMode;
    }

    public void setIsQueryMode(BooleanProperty isQueryMode) {
        this.isQueryMode = isQueryMode;
    }

    public StringProperty getTableName() {
        return tableName;
    }

    public void setTableName(StringProperty tableName) {
        this.tableName = tableName;
    }

    public StringProperty getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(StringProperty tableIndex) {
        this.tableIndex = tableIndex;
    }

    public ObservableList<MIRecord> getListInputTable() {
        return listInputTable;
    }

    public ObservableList<MIRecord> getListInputTableFilter() {
        return listInputTableFilter;
    }

    public ObservableList<MIRecord> getListOutputTable() {
        return listOutputTable;
    }

    public ObservableList<MIRecord> getListOutputMI() {
        return listOutputMI;
    }

    public ObservableList<Transaction> getListTransaction() {
        return listTransaction;
    }

    public DefaultRestClientMetadataConnection getRestClient() {
        return restClient;
    }

    public Service<String> getRestServicePostClearCache() {
        return restServicePostClearCache;
    }

    public RestGetTask getRestPostClearCache() {
        return restPostClearCache;
    }

    public StringProperty getSuffix() {
        return suffix;
    }

    public void setSuffix(StringProperty suffix) {
        this.suffix = suffix;
    }

    public BooleanProperty getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(BooleanProperty updateMode) {
        this.updateMode = updateMode;
    }
}
