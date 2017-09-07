/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view;

import com.kles.mi.MIPrograms;
import com.kles.mi.MIRecord;
import com.kles.mi.MIResult;
import com.kles.mi.NameValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class PromptF4ProgramController extends AbstractPromptF4Controller {

    @FXML
    private TableView<String> promptTable;

    @FXML
    private void initialize() {

    }

    @Override
    public void setResult(Object result) {
        listInd = new ArrayList();
        if (result instanceof MIPrograms) {
            listInd.stream().map((ref) -> {
                TableColumn<String, String> tableColumn = new TableColumn<>("Programs");
//                tableColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNameValue().get(ref).getValue()));
                return tableColumn;
            }).forEach((tableColumn) -> {
                promptTable.getColumns().add(tableColumn);
            });
            promptTable.setItems(FXCollections.observableArrayList(((MIPrograms) result).getName()));
        }
    }

    @FXML
    @Override
    protected void onMousePressed(MouseEvent event) {
        if (event.getClickCount() == 1) {
//            if (field != null) {
//                field.setText(promptTable.getSelectionModel().getSelectedItem().getNameValue().get(listInd.get(0)).getValue());
//            }

            listField.entrySet().stream().forEach((Map.Entry<String, Control> t) -> {
                if (t.getValue() != null) {
                    if (t.getValue() instanceof TextField) {
                        ((TextField) t.getValue()).setText(promptTable.getSelectionModel().getSelectedItem());
                    } else if (t.getValue() instanceof Label) {
                        ((Label) t.getValue()).setText(promptTable.getSelectionModel().getSelectedItem());
                    }

                }
            });

        }
    }
}
