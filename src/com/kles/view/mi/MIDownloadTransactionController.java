/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kles.MainApp;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.Transaction;
import com.kles.notification.Notification;
import com.kles.view.util.CheckListViewManageController;
import com.kles.view.util.FilePathChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.MaskerPane;

/**
 * FXML Controller class
 *
 * @author Jeremy.CHAUT
 */
public class MIDownloadTransactionController {

    @FXML
    private Label title, lpath;

    @FXML
    private FilePathChooser fpath;

    @FXML
    private CheckListViewManageController<Transaction> checkListTransactionController;

    @FXML
    private Button bOK, bCancel;

    private final Notification.Notifier notifier = Notification.Notifier.INSTANCE;
    private Stage stage;
    private MainApp mainApp;

    @FXML
    public void initialize() {
        HBox.setHgrow(checkListTransactionController.getRoot(), Priority.ALWAYS);
        VBox.setVgrow(checkListTransactionController.getList(), Priority.ALWAYS);
        checkListTransactionController.getList().setPrefHeight(200);
        bOK.disableProperty().bind(checkListTransactionController.getIsLeastOne().not().or(fpath.getPathField().textProperty().isEmpty()));
    }

    @FXML
    public void handleOK(ActionEvent e) {
        MaskerPane p = new MaskerPane();
        p.setText(mainApp.getResourceBundle().getString("mi.transaction.exporting"));
        Platform.runLater(() -> {
            stage.hide();
            Stage s = new Stage(StageStyle.UNDECORATED);
            s.setScene(new Scene(p));
            try {
                File f = new File(fpath.getFile().getAbsolutePath() + System.getProperty("file.separator") + "MITransactions.json");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String arrayToJson = objectMapper.writeValueAsString(checkListTransactionController.getList().getCheckModel().getCheckedItems());
                Files.write(Paths.get(f.getAbsolutePath()), arrayToJson.getBytes(), StandardOpenOption.CREATE);
                notifier.notify(new Notification(String.format(mainApp.getResourceBundle().getString("mi.transaction.export.success"), f.getAbsolutePath()), Notification.SUCCESS_ICON));
                s.close();
            } catch (JsonProcessingException ex) {
                FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("mi.export"), mainApp.getResourceBundle().getString("mi.transaction.export.error"), mainApp.getResourceBundle().getString("mi.transaction.export.error"), ex);
            } catch (IOException ex) {
                FxUtil.showAlert(Alert.AlertType.ERROR, mainApp.getResourceBundle().getString("mi.export"), mainApp.getResourceBundle().getString("mi.transaction.export.error"), mainApp.getResourceBundle().getString("mi.transaction.export.error"), ex);
            }
        });

    }

    @FXML
    public void handleCancel(ActionEvent e) {
        if (stage != null) {
            stage.close();
        }
    }

    public void setMainApp(MainApp main) {
        mainApp = main;
        checkListTransactionController.setTitle(mainApp.getResourceBundle().getString("mi.transaction.list"));
        checkListTransactionController.setLabel(mainApp.getResourceBundle().getString("mi.transaction.list.export"));
    }

    public void setTransactionList(List<Transaction> list) {
        checkListTransactionController.getListData().setAll(list);
        checkListTransactionController.selectAll();
        checkListTransactionController.getIsDisable().set(false);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
