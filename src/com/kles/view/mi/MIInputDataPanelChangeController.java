/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.mi.MIInputData;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIInputDataPanelChangeController {

    @FXML
    private TextField tpgm;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField ttransaction;

    @FXML
    private Button bOK;

    private MIInputData data;
    private MIInputPanel dataPanel;
    private BooleanProperty isClickedOK = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {
        // TODO
    }

    public void setData(MIInputData data) {
        this.data = data;
        tpgm.setText(data.getTransaction().getProgram());
        ttransaction.setText(data.getTransaction().getTransaction());
        this.dataPanel = MIInputPanel.create().build(data);
        scrollPane.setContent(dataPanel);
    }

    @FXML
    void handleOK(ActionEvent event) {
        dataPanel.getListControl().entrySet().forEach((Map.Entry<String, Control> t) -> {
            if (data.getData().containsKey(t.getKey())) {
                data.getData().put(t.getKey(), ((TextInputControl) t.getValue()).getText());
            }
        });
        isClickedOK.set(true);
    }

    public TextField getTpgm() {
        return tpgm;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public TextField getTtransaction() {
        return ttransaction;
    }

    public Button getbOK() {
        return bOK;
    }

    public BooleanProperty getIsClickedOK() {
        return isClickedOK;
    }
}
