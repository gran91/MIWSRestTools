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
import com.kles.fx.indicator.SimpleIndicator;
import com.kles.fx.indicator.SimpleIndicatorBuilder;
import com.kles.mi.MIPrograms;
import com.kles.model.IRestConnection;
import com.kles.model.MIWS;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import com.kles.view.util.ComboboxModelAddDisplay;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javax.ws.rs.core.MediaType;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIConnectionSimpleController {

    @FXML
    protected ComboboxModelAddDisplay comboEnv;

    @FXML
    protected HBox hboxButton;

    @FXML
    protected Button bconnect;

    @FXML
    protected ProgressIndicator progressConnect;

    @FXML
    protected Label message;

    protected SimpleIndicator control;
    protected SimpleObjectProperty<SimpleIndicator.IndicatorStyle> styleStatus;

    protected IRestConnection restConnection;
    protected BooleanProperty isConnected = new SimpleBooleanProperty(false);
    protected Service<String> restService;
    protected RestGetTask restTask;
    protected DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    protected MIPrograms miprograms;
    protected MainApp mainApp;

    @FXML
    public void initialize() {
        buildIncatorControler();
        restTask = new RestGetTask(restClient);
        createRestServiceProvider();
        addBinding();
    }

    protected void buildIncatorControler() {
        control = SimpleIndicatorBuilder.create().maxSize(10, 10).prefSize(10, 10).build();
        styleStatus = new SimpleObjectProperty<>(SimpleIndicator.IndicatorStyle.RED);
        hboxButton.getChildren().add(control);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(final long NOW) {
                control.indicatorStyleProperty().bind(styleStatus);
            }
        };
        timer.start();
    }

    protected void addBinding() {
        bconnect.disableProperty().bind(restService.runningProperty().or(comboEnv.getListModel().getSelectionModel().selectedIndexProperty().lessThan(0)));
        progressConnect.visibleProperty().bind(restService.runningProperty());
        message.visibleProperty().bind(isConnected.not());
        message.textProperty().bind(restService.messageProperty());
        message.textFillProperty().bind(Bindings.when(restService.stateProperty().isEqualTo(Worker.State.FAILED))
                .then(Color.RED).otherwise(Color.BLACK));
        isConnected.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                styleStatus.set(SimpleIndicator.IndicatorStyle.GREEN);
            } else {
                styleStatus.set(SimpleIndicator.IndicatorStyle.RED);
            }
        });
    }

    protected void createRestServiceProvider() {
        restService = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTask;
            }
        };
        restService.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("errorRest.title"), mainApp.getResourceBundle().getString("errorRest.header"), restTask.getMessage(), (Exception) restTask.getException());
                    isConnected.set(false);
                    break;
                case CANCELLED:
                    isConnected.set(false);
                    break;
                case RUNNING:
                    isConnected.set(false);
                    break;
                case SUCCEEDED:
                    if (!restTask.getValue().isEmpty()) {
                        ObjectMapper mapper = new XmlMapper();
                        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                        try {
                            miprograms = mapper.readValue(restTask.getValue(), MIPrograms.class);
                            isConnected.set(true);
                        } catch (Exception e) {
                            isConnected.set(false);
                        }
                    }
                    break;
            }
        }
        );
    }

    @FXML
    void connect(ActionEvent event) {
        runConnection((IRestConnection) comboEnv.getListModel().getSelectionModel().getSelectedItem());
    }

    protected void runConnection(IRestConnection ws) {
        restConnection = ws;
        restClient.setEnvironment(restConnection);
        restClient.setAction(DefaultRestClientMetadataConnection.LIST_API);
        GenericDataMIModel m = new GenericDataMIModel();
        restClient.setDataModel(m);
        restTask = new RestGetTask(restClient);
        restTask.setMediaType(MediaType.APPLICATION_XML);
        restService.restart();
    }

    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public BooleanProperty getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(BooleanProperty isConnected) {
        this.isConnected = isConnected;
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        comboEnv.setMainApp(mainApp);
        comboEnv.setModel(new MIWS());
        setEnvironmentList(mainApp.getDataMap().get("MIWS").getList());
    }

    public void setEnvironmentList(ObservableList list) {
        comboEnv.setList(list);
        comboEnv.init();
    }

    public MIPrograms getMIPrograms() {
        return miprograms;
    }

    public void setMIPrograms(MIPrograms miprograms) {
        this.miprograms = miprograms;
    }

    public Service<String> getRestService() {
        return restService;
    }

}
