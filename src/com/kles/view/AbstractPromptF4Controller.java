/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class AbstractPromptF4Controller {

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
    }

    @FXML
    protected void onMousePressed(MouseEvent event) {
        if (event.getClickCount() == 1) {
        }
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
