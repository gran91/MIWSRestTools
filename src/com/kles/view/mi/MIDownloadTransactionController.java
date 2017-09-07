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
import com.kles.mi.Transaction;
import com.kles.view.util.CheckListViewManageController;
import com.kles.view.util.FilePathChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.xml.bind.JAXBException;
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

    private Stage stage;
    private MainApp mainApp;

    @FXML
    public void initialize() {
        bOK.disableProperty().bind(checkListTransactionController.getIsLeastOne().not().or(fpath.getPathField().textProperty().isEmpty()));
    }

    @FXML
    public void handleOK(ActionEvent e) {
        MaskerPane p = new MaskerPane();
        p.setText("Exportation en cours...");
        Platform.runLater(() -> {
            stage.hide();
            Stage s = new Stage(StageStyle.UNDECORATED);
            s.setScene(new Scene(p));
//                JAXBContext jc = JAXBContext.newInstance(Wrapper.class, Transaction.class);
//                Marshaller marshaller = jc.createMarshaller();
            try {
                File f = new File(fpath.getFile().getAbsolutePath() + System.getProperty("file.separator") + "MITransactions.xml");
//                com.kles.jaxb.JAXBUtil.marshalList(marshaller, checkListTransactionController.getList().getCheckModel().getCheckedItems(), "transactions", f);
                ObjectMapper objectMapper = new ObjectMapper();
                //Set pretty printing of json
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String arrayToJson = objectMapper.writeValueAsString(checkListTransactionController.getList().getCheckModel().getCheckedItems());
                Files.write(Paths.get(f.getAbsolutePath()), arrayToJson.getBytes(), StandardOpenOption.CREATE);
                s.close();
            } catch (JsonProcessingException ex) {
                s.close();
                stage.show();
            } catch (IOException ex) {
                s.close();
                stage.show();
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
        checkListTransactionController.setTitle("Liste des transactions");
        checkListTransactionController.setLabel("Liste des transactions à exporter");
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
