/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.MIError;
import com.kles.mi.MIPrograms;
import com.kles.mi.MIResult;
import com.kles.model.mi.IRestClient;
import com.kles.task.rest.RestGetTask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.ws.rs.core.MediaType;
import org.controlsfx.control.PopOver;
import resources.Resource;

/**
 *
 * @author jchau
 */
public class PromptF4Util {

    public static int MODAL_DIALOG = 0;
    public static int POPUP = 1;
    private Service<String> restService;
    private RestGetTask restTask;
    protected PopOver popOver;
    protected double targetX;
    protected double targetY;
    private TextField field;
    private final int typeView;
    private final PromptF4Controller promptController;
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final HashMap<String, Control> listField;
    boolean autoPosition = false;
    private Scene scene;
    private int dataType = 0;
    public static int API_RECORD = 0;
    public static int API_PROGRAM = 1;
    public static int API_TRANSACTION = 2;

    public PromptF4Util(IRestClient rest, int dataType, String mediaType, HashMap<String, Control> listField, TextField field, int typeView) {
        this.typeView = typeView;
        this.field = field;
        this.listField = listField;
        this.dataType = dataType;
        restTask = new RestGetTask(rest, mediaType);
        promptController = showDataModelEditDialogStage();
        createService();
    }

    public PromptF4Util(IRestClient rest, int dataType, HashMap<String, Control> listField, TextField field, int typeView) {
        this(rest, dataType, MediaType.APPLICATION_JSON, listField, field, typeView);
    }

    public PromptF4Util(IRestClient rest, HashMap<String, Control> listField, TextField field, int typeView) {
        this(rest, API_RECORD, MediaType.APPLICATION_JSON, listField, field, typeView);
    }

    public void show() {
        if (typeView == POPUP) {
            promptF4Popup();
        }
        restService.restart();
    }

    public PromptF4Controller showDataModelEditDialogStage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PromptF4.fxml"));
            StackPane page = (StackPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("F4");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner();
            dialogStage.getIcons().add(Resource.LOGO_ICON_32);
            scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            dialogStage.setScene(scene);
            PromptF4Controller controller = loader.getController();
//            AbstractPromptF4Controller controller = loader.getController();;
//            switch (dataType) {
//                case 0:
//                    PromptF4ResultController controllerResult = loader.getController();
//                    listField.entrySet().stream().forEach(new Consumer<Map.Entry<String, Control>>() {
//                        @Override
//                        public void accept(Map.Entry<String, Control> t) {
//                            controllerResult.addColumn(t.getKey());
//                        }
//                    });
//                    controller = controllerResult;
//                    break;
//                case 1:
//                    controller = new PromptF4ProgramController();
//                    loader.setController(controller);
//                    break;
//            }

            controller.setStage(dialogStage);
            controller.getProgress().visibleProperty().bind(isRunning);

            listField.entrySet().stream().forEach((Map.Entry<String, Control> t) -> {
                controller.addColumn(t.getKey());
            });
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void promptF4Popup() {
        try {
            if (popOver != null && popOver.isShowing()) {
                popOver.hide(Duration.ZERO);
            }
            com.sun.glass.ui.Robot robot = com.sun.glass.ui.Application.GetApplication().createRobot();
            int x = robot.getMouseX();
            int y = robot.getMouseY();
            popOver = createPopOver();
            if (autoPosition) {
                targetX = field.getScene().getX() + field.getScene().getWindow().getX() + field.getLayoutX() + field.getWidth();
                targetY = field.getScene().getY() + field.getScene().getWindow().getY() + field.getLayoutY() + field.getHeight();
                popOver.show(field, targetX, targetY);
            } else {
                targetX = x;
                targetY = y;
                popOver.show(field, targetX, targetY);
            }
        } catch (IOException ex) {
            Logger.getLogger(PromptF4ResultController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected PopOver createPopOver() throws IOException {
        popOver = new PopOver();
        popOver.setDetachable(false);
        popOver.setDetached(false);
        popOver.setArrowSize(10);
        promptController.setTextField(field);
        promptController.setListField(listField);
        popOver.setContentNode(promptController.getStage().getScene().getRoot());
        /*controller.isClickedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue && !controller.hasError()) {
                popOver.hide();
            } else {
                popOver.show(faciField);
            }
        });*/
        return popOver;
    }

    private void createService() {
        restService = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTask;
            }
        };
        restService.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case FAILED:
                    FxUtil.showAlert(Alert.AlertType.ERROR, MainApp.resourceMessage.getString("errorRest.title"), MainApp.resourceMessage.getString("errorRest.header"), restTask.getMessage());
                    isRunning.setValue(false);
                    break;
                case CANCELLED:
                    isRunning.setValue(false);
                    break;
                case RUNNING:
                    isRunning.setValue(true);
                    break;
                case SUCCEEDED:
                    if (!restTask.getValue().isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            MIResult res = mapper.readValue(restTask.getValue(), MIResult.class);
                            promptController.setResult(res);
                            isRunning.setValue(false);
                        } catch (Exception e) {
                            try {
                                MIPrograms res1 = mapper.readValue(restTask.getValue(), MIPrograms.class);
                                promptController.setResult(res1);
                                isRunning.setValue(false);
                                break;
                            } catch (Exception ex) {
                                try {
                                    MIError err = mapper.readValue(restTask.getValue(), MIError.class);
                                    FxUtil.showAlert(Alert.AlertType.ERROR, MainApp.resourceMessage.getString("error.API"), err.getType(), err.getMessage());
                                    isRunning.setValue(false);
                                    break;
                                } catch (IOException ex2) {
                                    FxUtil.showAlert(Alert.AlertType.ERROR, MainApp.resourceMessage.getString("error.API"), MainApp.resourceMessage.getString("error.unknown"), MainApp.resourceMessage.getString("error.unknown"));
                                    isRunning.setValue(false);
                                    break;
                                }
                            }
                        }
                    }
                    isRunning.setValue(false);
                    break;
            }
        });
        // message.textProperty().bind(restService.messageProperty());
    }

    public BooleanProperty isRunning() {
        return isRunning;
    }
}
