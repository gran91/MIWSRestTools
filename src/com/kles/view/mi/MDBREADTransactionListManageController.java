/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.MIProgramMetadata;
import com.kles.mi.MIRecord;
import com.kles.mi.MIResult;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import com.kles.utils.MIUtils;
import com.kles.view.util.PanelIndicator;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.ws.rs.core.MediaType;
import org.controlsfx.control.textfield.TextFields;
import resources.Resource;
import resources.ResourceApp;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MDBREADTransactionListManageController {

    @FXML
    private StackPane root;

    @FXML
    private ListView<Transaction> transactionList;

//    @FXML
    private TextField tfiltertransaction;

    private PanelIndicator progressListTransaction;

    @FXML
    private Button bNew, bRemoveTransaction, bEditTransaction, bExport, bCreateGet, bCreateLst, bCreateSel;

    @FXML
    private HBox searchBar, buttonBar;

    private String transactionName = "";
    private final ObservableList<Transaction> listTransaction = FXCollections.observableArrayList();
    private FilteredList<Transaction> filteredTransaction;

    private IRestConnection restConnection;
    private Service<String> restServiceListTransaction, restServiceDeleteTransaction, restServicePostClearCache;
    private RestGetTask restTask, restTaskDelTrans, restPostClearCache;
    private final DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private Transaction transaction;
    private MIResult result;
    private MIProgramMetadata miprogram;
    private MainApp mainApp;
    private Stage stage;
    private int cptDelTrans;
    private List<Transaction> listDelTrans;
    private final BooleanProperty isNeedToClean = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {
        createRestServicePostClearCache();
        createRestServiceListTransaction();
        createRestServiceDeleteTransaction();

        isNeedToClean.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue != null) {
                if (newValue) {
                    runClearCache();
                }
            }
        });

        tfiltertransaction = TextFields.createClearableTextField();
        HBox.setHgrow(tfiltertransaction, Priority.ALWAYS);
        filteredTransaction = new FilteredList(listTransaction, s -> true);
        tfiltertransaction.textProperty().addListener(obs -> {
            String filter = tfiltertransaction.getText();
            if (filter == null || filter.length() == 0) {
                filteredTransaction.setPredicate(s -> true);
            } else {
                filteredTransaction.setPredicate(s -> s.getTransaction().contains(filter));
            }
        });
        searchBar.getChildren().add(tfiltertransaction);

        progressListTransaction = PanelIndicator.create().build().setService(restServiceListTransaction);
        root.getChildren().add(progressListTransaction.getPanel());
        progressListTransaction.getPanel().visibleProperty().bind(restServiceDeleteTransaction.runningProperty().or(restServiceListTransaction.runningProperty()));
        transactionList.disableProperty().bind(Bindings.isEmpty(listTransaction));

        transactionList.setItems(filteredTransaction);
        /*TRANSACTION*/
        transactionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bEditTransaction.disableProperty().bind(transactionList.getSelectionModel().selectedItemProperty().isNull().or(restServiceListTransaction.runningProperty()));
        bRemoveTransaction.disableProperty().bind(transactionList.getSelectionModel().selectedItemProperty().isNull().or(restServiceListTransaction.runningProperty()));
        bExport.disableProperty().bind(transactionList.getSelectionModel().selectedItemProperty().isNull().or(restServiceListTransaction.runningProperty()));
        tfiltertransaction.disableProperty().bind(Bindings.isEmpty(listTransaction));
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
                    runListTransaction();
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restPostClearCache.getMessage());
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    runListTransaction();
                    break;
            }
        });
    }

    private void createRestServiceListTransaction() {
        restServiceListTransaction = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTask;
            }
        };
        restServiceListTransaction.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    listTransaction.clear();
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTask.getMessage());
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    if (!restTask.getValue().isEmpty()) {
                        ObjectMapper mapper = new XmlMapper();
                        try {
                            miprogram = mapper.readValue(restTask.getValue(), MIProgramMetadata.class);
                            listTransaction.setAll(MIUtils.purify(miprogram.getTransactions()));
//                            Platform.runLater(() -> {
//                                transactionList.setItems(FXCollections.observableList(miprogram.getTransactions()));
//                            });
                        } catch (Exception e) {
//                            transactionList.setItems(null);
                            listTransaction.clear();
                            FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), e.getMessage());
                        }
                    }
                    break;
            }
        }
        );
    }

    private void delTransOrList() {
        if (cptDelTrans < listDelTrans.size()) {
            runDeleteTransaction(listDelTrans.get(cptDelTrans));
            cptDelTrans++;
        } else {
            runClearCache();
        }
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
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTaskDelTrans.getMessage());
                    delTransOrList();
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    delTransOrList();
                    break;
            }
        }
        );
    }

    @FXML
    private void removeTransaction(ActionEvent event) {
        if (transactionList.getSelectionModel().getSelectedItem() != null) {
            cptDelTrans = 0;
            listDelTrans = transactionList.getSelectionModel().getSelectedItems();
//            runDeleteTransaction(listDelTrans.get(cptDelTrans));
            delTransOrList();
        }
    }

    @FXML
    private void editTransaction(ActionEvent event) {
        if (transactionList.getSelectionModel().getSelectedItem() != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    showMDBREAD(transactionList.getSelectionModel().getSelectedItem());
                }
            });
        }
    }

    private void runDeleteTransaction(Transaction t) {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("DelTransaction");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", t.getTransaction());
        restClient.setDataModel(m);
        restTaskDelTrans = new RestGetTask(restClient);
        restTaskDelTrans.setMediaType(MediaType.APPLICATION_JSON);
        restServiceDeleteTransaction.restart();
    }

    public static String getDescriptionTransactionFromKeys(List<MIRecord> list) {
        String s = "Keys:";
        s = list.stream().map((r) -> (r.getNameValue().get(0).getValue().trim().length() == 6) ? r.getNameValue().get(0).getValue().trim().substring(2) : r.getNameValue().get(0).getValue().trim()).map((key) -> " " + key).reduce(s, String::concat);
        return s;
    }

    public static String getTransactionTypeFromMethod(String typeMethod) {
        String type = "S";
        if (typeMethod.equals("Lst") || typeMethod.equals("Sel")) {
            type = "M";
        }
        return type;
    }

    public void runClearCache() {
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
    private void export(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(mainApp.getResourceBundle());
            loader.setLocation(MDBREADTransactionListManageController.class.getResource("/com/kles/view/mi/MIExport.fxml"));
            Pane miRestTest = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Export");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.getIcons().add(Resource.LOGO_ICON_32);

            MIExportController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setStage(dialogStage);
            controller.setTransactionList(transactionList.getSelectionModel().getSelectedItems());
            controller.setEnvironmentList(mainApp.getDataMap().get("MIWS").getList());
            Scene scene = new Scene(miRestTest, Color.TRANSPARENT);
            scene.getStylesheets().add(MainApp.class.getResource("application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MDBREADTransactionListManageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runListTransaction() {
        restClient.setAction(DefaultRestClientMetadataConnection.LIST_TRANS);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MDBREADMI");
        restClient.setDataModel(m);
        restTask = new RestGetTask(restClient);
        restTask.setMediaType(MediaType.APPLICATION_XML);
        restServiceListTransaction.restart();
    }

    public void showMDBREAD(Transaction t) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(mainApp.getResourceBundle());
            loader.setLocation(MDBREADTransactionListManageController.class.getResource("/com/kles/view/mi/MDBREADEdit.fxml"));
            Pane miRestTest = loader.load();

            Stage stage = new Stage();
            stage.setTitle("MDBREAD");
            stage.initModality(Modality.NONE);
            stage.initOwner(mainApp.getPrimaryStage());
            stage.getIcons().add(ResourceApp.LOGO_ICON_32);
            Scene scene = new Scene(miRestTest);
            stage.setScene(scene);

            MDBREADEditController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setRestConnection(restConnection);
            controller.setTransaction(t);
            isNeedToClean.bind(Bindings
                    .when(controller.getMdbreadListManageController().getCptFieldsProperty()
                            .isEqualTo(controller.getMdbreadListManageController().getListInputTableFilter().size() + controller.getMdbreadListManageController().getListOutputMI().size()))
                    .then(true).otherwise(false));
            controller.getMdbreadListManageController().getListTransaction().setAll(listTransaction);
            listTransaction.addListener((ListChangeListener.Change<? extends Transaction> c) -> {
                controller.getMdbreadListManageController().getListTransaction().setAll(listTransaction);
            });

            controller.setStage(stage);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MDBREADTransactionListManageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public StackPane getLeft() {
        return root;
    }

    public void setLeft(StackPane left) {
        this.root = left;
    }

    public ListView<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(ListView<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public TextField getTfiltertransaction() {
        return tfiltertransaction;
    }

    public void setTfiltertransaction(TextField tfiltertransaction) {
        this.tfiltertransaction = tfiltertransaction;
    }

    public PanelIndicator getProgressListTransaction() {
        return progressListTransaction;
    }

    public void setProgressListTransaction(PanelIndicator progressListTransaction) {
        this.progressListTransaction = progressListTransaction;
    }

    public Button getbNew() {
        return bNew;
    }

    public void setbNew(Button bNew) {
        this.bNew = bNew;
    }

    public Button getbRemoveTransaction() {
        return bRemoveTransaction;
    }

    public void setbRemoveTransaction(Button bRemoveTransaction) {
        this.bRemoveTransaction = bRemoveTransaction;
    }

    public Button getbExport() {
        return bExport;
    }

    public void setbExport(Button bExport) {
        this.bExport = bExport;
    }

    public Button getbCreateGet() {
        return bCreateGet;
    }

    public void setbCreateGet(Button bCreateGet) {
        this.bCreateGet = bCreateGet;
    }

    public Button getbCreateLst() {
        return bCreateLst;
    }

    public void setbCreateLst(Button bCreateLst) {
        this.bCreateLst = bCreateLst;
    }

    public Button getbCreateSel() {
        return bCreateSel;
    }

    public void setbCreateSel(Button bCreateSel) {
        this.bCreateSel = bCreateSel;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public FilteredList<Transaction> getFilteredTransaction() {
        return filteredTransaction;
    }

    public void setFilteredTransaction(FilteredList<Transaction> filteredTransaction) {
        this.filteredTransaction = filteredTransaction;
    }

    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public Service<String> getRestServiceListTransaction() {
        return restServiceListTransaction;
    }

    public void setRestServiceListTransaction(Service<String> restServiceListTransaction) {
        this.restServiceListTransaction = restServiceListTransaction;
    }

    public Service<String> getRestServiceDeleteTransaction() {
        return restServiceDeleteTransaction;
    }

    public void setRestServiceDeleteTransaction(Service<String> restServiceDeleteTransaction) {
        this.restServiceDeleteTransaction = restServiceDeleteTransaction;
    }

    public Service<String> getRestServicePostClearCache() {
        return restServicePostClearCache;
    }

    public void setRestServicePostClearCache(Service<String> restServicePostClearCache) {
        this.restServicePostClearCache = restServicePostClearCache;
    }

    public RestGetTask getRestTask() {
        return restTask;
    }

    public void setRestTask(RestGetTask restTask) {
        this.restTask = restTask;
    }

    public RestGetTask getRestTaskDelTrans() {
        return restTaskDelTrans;
    }

    public void setRestTaskDelTrans(RestGetTask restTaskDelTrans) {
        this.restTaskDelTrans = restTaskDelTrans;
    }

    public RestGetTask getRestPostClearCache() {
        return restPostClearCache;
    }

    public void setRestPostClearCache(RestGetTask restPostClearCache) {
        this.restPostClearCache = restPostClearCache;
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

    public int getCptDelTrans() {
        return cptDelTrans;
    }

    public void setCptDelTrans(int cptDelTrans) {
        this.cptDelTrans = cptDelTrans;
    }

    public List<Transaction> getListDelTrans() {
        return listDelTrans;
    }

    public void setListDelTrans(List<Transaction> listDelTrans) {
        this.listDelTrans = listDelTrans;
    }

    public ObservableList<Transaction> getListTransaction() {
        return listTransaction;
    }

    public DefaultRestClientMetadataConnection getRestClient() {
        return restClient;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public Stage getStage() {
        return stage;
    }

}
