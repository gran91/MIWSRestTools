/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.model.MIWS;
import com.kles.view.util.ComboboxModelAddDisplay;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIConnectionController extends MIConnectionSimpleController {

    @FXML
    private TextField tport;

    @FXML
    private PasswordField tpassword;

    @FXML
    private TextField tlogin;

    @FXML
    private ComboboxModelAddDisplay comboEnv;

    @FXML
    private TextField thost;

    @Override
    protected void addBinding() {
        super.addBinding();
        comboEnv.getListModel().valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            thost.setText(((MIWS) newValue).getHost());
            tport.setText("" + ((MIWS) newValue).getPort());
            tlogin.setText(((MIWS) newValue).getLogin());
            tpassword.setText(((MIWS) newValue).getPassword());
        });
    }

    @FXML
    @Override
    void connect(ActionEvent event) {
        MIWS ws = new MIWS();
        ws.setHost(thost.getText());
        ws.setPort(Integer.parseInt(tport.getText()));
        ws.setLogin(tlogin.getText());
        ws.setPassword(tpassword.getText());
        super.runConnection(ws);
    }
}
