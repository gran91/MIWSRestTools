package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.mi.Transaction;
import com.kles.model.MIWS;
import com.kles.view.util.CheckListViewManageController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIExportController {

    @FXML
    private Label title;

    @FXML
    Label lenvironment;

    @FXML
    private TitledPane titlePaneTransaction;

    @FXML
    private CheckListViewManageController<Transaction> checkListTransactionController;

    @FXML
    private TitledPane titlePaneEnvController;

    @FXML
    private CheckListViewManageController<MIWS> checkListEnvController;

    @FXML
    private ComboBox<String> taction;

    @FXML
    private Button bCancel;

    @FXML
    private Button bOK;

    private MainApp mainApp;
    private Stage stage;
    private int cpt = 0;
    private final ObservableList<String> listAction = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        HBox.setHgrow(checkListTransactionController.getRoot(), Priority.ALWAYS);
        VBox.setVgrow(checkListTransactionController.getList(), Priority.ALWAYS);
        checkListTransactionController.getList().setPrefHeight(200);
        HBox.setHgrow(checkListEnvController.getRoot(), Priority.ALWAYS);
        VBox.setVgrow(checkListEnvController.getList(), Priority.ALWAYS);
        checkListEnvController.getList().setPrefHeight(200);
        taction.setItems(listAction);
        bOK.disableProperty().bind(checkListTransactionController.getIsLeastOne().and(checkListEnvController.getIsLeastOne()).not());
    }

    @FXML
    void handleCancel(ActionEvent event) {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void handleOK(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(mainApp.getResourceBundle());
            loader.setLocation(MIExportController.class.getResource("/com/kles/view/mi/MIExportProgressView.fxml"));
            Pane mitaskprog = loader.load();
            MIExportProgressViewController controller = loader.getController();
            controller.setListTask(buildTaskList());
            Platform.runLater(() -> {
                stage.setScene(new Scene(mitaskprog));
                controller.run();
            });
        } catch (IOException ex) {
            Logger.getLogger(MDBREADTransactionListManageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<MIExportTask> buildTaskList() {
        cpt = 0;
        List<MIExportTask> list = new ArrayList<>();
        checkListEnvController.getList().getCheckModel().getCheckedItems().forEach((MIWS miws) -> {
            checkListTransactionController.getList().getCheckModel().getCheckedItems().forEach((Transaction t) -> {
                final MIExportTask task = new MIExportTask(cpt, t, miws);
                task.setMainApp(mainApp);
                task.setUpdateType(taction.getSelectionModel().getSelectedIndex());
                list.add(task);
                cpt++;
            });
        });

        return list;
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        checkListTransactionController.setTitle(mainApp.getResourceBundle().getString("mi.transaction.list"));
        checkListTransactionController.setLabel(mainApp.getResourceBundle().getString("mi.transaction.list.export"));
        checkListEnvController.setTitle(mainApp.getResourceBundle().getString("mi.environment.list"));
        checkListEnvController.setLabel(mainApp.getResourceBundle().getString("mi.environment.list.update"));
        listAction.add(mainApp.getResourceBundle().getString("mi.transaction.action.update"));
        listAction.add(mainApp.getResourceBundle().getString("mi.transaction.action.skip"));
        taction.getSelectionModel().select(0);
    }

    public void setTransactionList(List<Transaction> list) {
        checkListTransactionController.getListData().setAll(list);
        checkListTransactionController.selectAll();
        checkListTransactionController.getIsDisable().set(false);
    }

    public void setEnvironmentList(List<MIWS> list) {
        checkListEnvController.getListData().setAll(list);
        checkListEnvController.selectAll();
        checkListEnvController.getIsDisable().set(false);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
