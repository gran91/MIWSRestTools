/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view;

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
public class PromptF4Controller {

    @FXML
    private TableView<MIRecord> promptTable;
    @FXML
    protected ProgressIndicator progress;

    protected final ArrayList<String> listRef = new ArrayList<>();
    protected ArrayList<Integer> listInd = new ArrayList<>();
    protected Stage dialogStage;
    protected TextField field;
    protected HashMap<String, Control> listField;

    @FXML
    private void initialize() {

    }

    public void addColumn(String s) {
        listRef.add(s);
    }

    public void setResult(Object result) {
        listInd = new ArrayList();
        if (result instanceof MIResult) {
            for (int i = 0; i < ((MIResult) result).getMetadata().getField().size(); i++) {
                if (listRef.contains(((MIResult) result).getMetadata().getField().get(i).getName())) {
                    listInd.add(i);
                }
            }

            listInd.stream().map((ref) -> {
                TableColumn<MIRecord, String> tableColumn = new TableColumn<>(((MIResult) result).getMetadata().getField().get(ref).getDescription());
                tableColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNameValue().get(ref).getValue()));
                return tableColumn;
            }).forEach((tableColumn) -> {
                promptTable.getColumns().add(tableColumn);
            });
            promptTable.setItems(FXCollections.observableArrayList(((MIResult) result).getMIRecord()));
        }
    }

    @FXML
    protected void onMousePressed(MouseEvent event) {
        if (event.getClickCount() == 1) {
            listField.entrySet().stream().forEach((Map.Entry<String, Control> t) -> {
                if (t.getValue() != null) {
                    if (t.getValue() instanceof TextField) {
                        ((TextField) t.getValue()).setText(promptTable.getSelectionModel().getSelectedItem().getNameValue().get(getIndex(t.getKey())).getValue());
                    } else if (t.getValue() instanceof Label) {
                        ((Label) t.getValue()).setText(promptTable.getSelectionModel().getSelectedItem().getNameValue().get(getIndex(t.getKey())).getValue());
                    }

                }
            });

        }
    }

    private int getIndex(String key) {
        int ind = 0;
        for (NameValue n : promptTable.getSelectionModel().getSelectedItem().getNameValue()) {
            if (n.getName().equals(key)) {
                break;
            }
            ind++;
        }
        return ind;
    }

    public void setStage(Stage stage) {
        dialogStage = stage;
    }

    public Stage getStage() {
        return dialogStage;
    }

    public void setTextField(TextField text) {
        field = text;
    }

    public ProgressIndicator getProgress() {
        return progress;
    }

    public void setListField(HashMap<String, Control> listField) {
        this.listField = listField;
    }
}
