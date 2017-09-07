/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.mi.Transaction;
import com.kles.model.MIWS;
import com.kles.view.util.CheckListViewManageController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
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
    private TitledPane titlePaneTransaction;

    @FXML
    private CheckListViewManageController<Transaction> checkListTransactionController;

    @FXML
    private TitledPane titlePaneEnvController;

    @FXML
    private CheckListViewManageController<MIWS> checkListEnvController;

    @FXML
    private Button bCancel;

    @FXML
    private Button bOK;

    private MainApp mainApp;
    private Stage stage;
    private int cpt = 0;

    @FXML
    public void initialize() {
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
        checkListEnvController.getList().getCheckModel().getCheckedItems().forEach(new Consumer<MIWS>() {
            @Override
            public void accept(MIWS miws) {
                checkListTransactionController.getList().getCheckModel().getCheckedItems().forEach(new Consumer<Transaction>() {
                    @Override
                    public void accept(Transaction t) {
                        final MIExportTask task = new MIExportTask(cpt);
                        task.setRestConnection(miws);
                        task.setTransaction(t);
                        list.add(task);
                        cpt++;
                    }
                });
            }
        });

        return list;
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        checkListTransactionController.setTitle("Liste des transactions");
        checkListTransactionController.setLabel("Liste des transactions à exporter");
        checkListEnvController.setTitle("Liste des environnements");
        checkListEnvController.setLabel("Liste des environnements à mettre à jour");
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
