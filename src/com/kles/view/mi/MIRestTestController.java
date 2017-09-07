/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.MIInputData;
import com.kles.mi.MIResult;
import com.kles.mi.MIResultInput;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import com.kles.utils.MIUtils;
import com.kles.view.util.PanelIndicator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.ws.rs.core.MediaType;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIRestTestController {

    @FXML
    private BorderPane root;

    @FXML
    private MIResultTableViewController miResultTableViewController;

    @FXML
    private VBox left, center;

    @FXML
    private ScrollPane scrollInput;

    @FXML
    private Button bRun, bClean, bSaveIn, bCloseAll, bSaveAllDisable;

    @FXML
    private ProgressIndicator progressRun;

    @FXML
    private TabPane tabPane;
    private BooleanProperty isInputPanelDisable, isTabPaneDisable, isRunDisable, isCleanDisable, isSaveDisable, isCloseAllDisable, isSaveAllDisable;
    private MIInputPanel inputPanel = new MIInputPanel();
    private IntegerProperty maxRecord = new SimpleIntegerProperty(0);
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
        isInputPanelDisable = new SimpleBooleanProperty(true);
        isRunDisable = new SimpleBooleanProperty(true);
        isCleanDisable = new SimpleBooleanProperty(true);
        isSaveDisable = new SimpleBooleanProperty(true);
        isTabPaneDisable = new SimpleBooleanProperty(true);

        inputPanel.disableProperty().bind(isInputPanelDisable);
        bRun.disableProperty().bind(isRunDisable);
        bClean.disableProperty().bind(isCleanDisable);
        bSaveIn.disableProperty().bind(isSaveDisable);
        tabPane.disableProperty().bind(isTabPaneDisable);

        createRestServiceProvider();
        progressRun.visibleProperty().bind(restService.runningProperty());
    }

    public void buildInputPanelData(Transaction t, LinkedHashMap<String, String> data) {
        inputPanel = new MIInputPanel();
        inputPanel.build(t.getInput());
        inputPanel.setData(data);
        scrollInput.setContent(inputPanel);
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
                    isRunDisable.set(false);
                    switchScrollPaneContent();
                    break;
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTask.getMessage());
                    isRunDisable.set(false);
                    switchScrollPaneContent();
                    break;
                case CANCELLED:
                    isRunDisable.set(false);
                    switchScrollPaneContent();
                    break;
                case RUNNING:
                    isRunDisable.set(true);
                    switchScrollPaneContent();
                    break;
                case SUCCEEDED:
                    isTabPaneDisable.setValue(false);
                    if (!restTask.getValue().isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            result = mapper.readValue(restTask.getValue(), MIResult.class);
                            final MIInputData inputData = new MIInputData();
                            inputData.setRestConnection(restConnection);
                            inputData.setTransaction(transaction);
                            inputData.setData(MIInputPanel.getDataFromPanel(inputPanel));
                            final MIResultInput inputResult = new MIResultInput();
                            inputResult.setInputData(inputData);
                            inputResult.setResult(result);
                            final Tab t = new Tab("Waiting...", PanelIndicator.create().build().getPanel());
                            tabPane.getTabs().add(t);
                            Platform.runLater(() -> {
                                addMITableViewResult(inputResult, t);
                            });
                        } catch (Exception e) {
                            try {
                                MIUtils.showMIError(restService.getValue());
                            } catch (IOException ex) {
                                FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("error.API"), mainApp.getResourceBundle().getString("error.unknown"), mainApp.getResourceBundle().getString("error.unknown"));
                            }
                        }
                    }
                    isRunDisable.set(false);
                    switchScrollPaneContent();
                    break;
            }
        }
        );
    }

    private void switchScrollPaneContent() {
        if (restService.getState().equals(Worker.State.RUNNING)) {
            final VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.setSpacing(10);
            vbox.setPrefSize(347, 595);
            final ProgressIndicator prog = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
            final Label label = new Label();
            label.setPrefSize(345, 40);
            label.setAlignment(Pos.CENTER);
            label.textProperty().bind(restService.messageProperty());
            vbox.getChildren().add(prog);
            vbox.getChildren().add(label);
            scrollInput.setContent(vbox);
        } else {
            scrollInput.setContent(inputPanel);
        }
    }

    private void addMITableViewResult(MIResultInput resultInput, Tab t) {
//        final Tab tab = new Tab();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(mainApp.getResourceBundle());
            loader.setLocation(MIRestTestController.class.getResource("/com/kles/view/mi/MIResultTableView.fxml"));
            Pane node = (Pane) loader.load();
            MIResultTableViewController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setMIData(resultInput);
            controller.buildTable();
//            tab.setContent(node);
            t.setContent(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        tab.setText(resultInput.getInputData().getTransaction().getProgram() + "/" + resultInput.getInputData().getTransaction().getTransaction());
        t.setText(resultInput.getInputData().getTransaction().getProgram() + "/" + resultInput.getInputData().getTransaction().getTransaction());
//        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
//        root.setCenter(tabPane);
    }

    @FXML
    public void clean() {
        if (inputPanel != null) {
            inputPanel.getListControl().entrySet().forEach(new Consumer<Map.Entry<String, Control>>() {
                @Override
                public void accept(Map.Entry<String, Control> t) {
                    if (t.getValue() instanceof TextInputControl) {
                        ((TextInputControl) t.getValue()).setText("");
                    }
                }
            });
        }
    }

    @FXML
    public void saveIn() {
        if (inputPanel != null) {
            final MIInputData inputData = new MIInputData();
            inputData.setRestConnection(restConnection);
            inputData.setTransaction(transaction);
            LinkedHashMap<String, String> data = MIInputPanel.getDataFromPanel(inputPanel);
            inputData.setData(data);
            if (data.size() > 0) {
                mainApp.getDataMap().get("MIInputData").getList().add(inputData);
            }
        }
    }

    @FXML
    public void closeAll() {
        tabPane.getTabs().clear();
    }

    @FXML
    public void run() {
        restClient.setEnvironment(restConnection);
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        restClient.setMaxRecord(maxRecord.get());
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram(transaction.getProgram());
        m.setMITransaction(transaction.getTransaction());
        m.setInputData(MIInputPanel.getDataFromPanel(inputPanel));
        restClient.setDataModel(m);
        restTask = new RestGetTask(restClient);
        restTask.setMediaType(MediaType.APPLICATION_JSON);
        restService.restart();
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDataToInputPanel(Transaction newValue) {
        if (newValue != null) {
            transaction = newValue;
            inputPanel = new MIInputPanel();
            inputPanel.build(newValue.getInput());
            scrollInput.setContent(inputPanel);
            isInputPanelDisable.set(false);
            isRunDisable.set(false);
            isCleanDisable.set(false);
            isSaveDisable.set(false);
        } else {
            transaction = null;
            scrollInput.setContent(null);
            isInputPanelDisable.set(true);
            isRunDisable.set(true);
            isCleanDisable.set(true);
            isSaveDisable.set(true);
        }
    }

    public BorderPane getRoot() {
        return root;
    }

    public void setRoot(BorderPane root) {
        this.root = root;
    }

    public MIResultTableViewController getMiResultTableViewController() {
        return miResultTableViewController;
    }

    public void setMiResultTableViewController(MIResultTableViewController miResultTableViewController) {
        this.miResultTableViewController = miResultTableViewController;
    }

    public VBox getLeft() {
        return left;
    }

    public void setLeft(VBox left) {
        this.left = left;
    }

    public VBox getCenter() {
        return center;
    }

    public void setCenter(VBox center) {
        this.center = center;
    }

    public ScrollPane getScrollInput() {
        return scrollInput;
    }

    public void setScrollInput(ScrollPane scrollInput) {
        this.scrollInput = scrollInput;
    }

    public Button getbRun() {
        return bRun;
    }

    public void setbRun(Button bRun) {
        this.bRun = bRun;
    }

    public Button getbClean() {
        return bClean;
    }

    public void setbClean(Button bClean) {
        this.bClean = bClean;
    }

    public Button getbSaveIn() {
        return bSaveIn;
    }

    public void setbSaveIn(Button bSaveIn) {
        this.bSaveIn = bSaveIn;
    }

    public Button getbCloseAll() {
        return bCloseAll;
    }

    public void setbCloseAll(Button bCloseAll) {
        this.bCloseAll = bCloseAll;
    }

    public Button getbSaveAllDisable() {
        return bSaveAllDisable;
    }

    public void setbSaveAllDisable(Button bSaveAllDisable) {
        this.bSaveAllDisable = bSaveAllDisable;
    }

    public ProgressIndicator getProgressRun() {
        return progressRun;
    }

    public void setProgressRun(ProgressIndicator progressRun) {
        this.progressRun = progressRun;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public BooleanProperty getIsInputPanelDisable() {
        return isInputPanelDisable;
    }

    public void setIsInputPanelDisable(BooleanProperty isInputPanelDisable) {
        this.isInputPanelDisable = isInputPanelDisable;
    }

    public BooleanProperty getIsTabPaneDisable() {
        return isTabPaneDisable;
    }

    public void setIsTabPaneDisable(BooleanProperty isTabPaneDisable) {
        this.isTabPaneDisable = isTabPaneDisable;
    }

    public BooleanProperty getIsRunDisable() {
        return isRunDisable;
    }

    public void setIsRunDisable(BooleanProperty isRunDisable) {
        this.isRunDisable = isRunDisable;
    }

    public BooleanProperty getIsCleanDisable() {
        return isCleanDisable;
    }

    public void setIsCleanDisable(BooleanProperty isCleanDisable) {
        this.isCleanDisable = isCleanDisable;
    }

    public BooleanProperty getIsSaveDisable() {
        return isSaveDisable;
    }

    public void setIsSaveDisable(BooleanProperty isSaveDisable) {
        this.isSaveDisable = isSaveDisable;
    }

    public BooleanProperty getIsCloseAllDisable() {
        return isCloseAllDisable;
    }

    public void setIsCloseAllDisable(BooleanProperty isCloseAllDisable) {
        this.isCloseAllDisable = isCloseAllDisable;
    }

    public BooleanProperty getIsSaveAllDisable() {
        return isSaveAllDisable;
    }

    public void setIsSaveAllDisable(BooleanProperty isSaveAllDisable) {
        this.isSaveAllDisable = isSaveAllDisable;
    }

    public MIInputPanel getInputPanel() {
        return inputPanel;
    }

    public void setInputPanel(MIInputPanel inputPanel) {
        this.inputPanel = inputPanel;
    }

    public IntegerProperty getMaxRecord() {
        return maxRecord;
    }

    public void setMaxRecord(IntegerProperty maxRecord) {
        this.maxRecord = maxRecord;
    }

    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
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

    public MainApp getMainApp() {
        return mainApp;
    }

    public Stage getStage() {
        return stage;
    }

}
