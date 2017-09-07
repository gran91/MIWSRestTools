/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model;

import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MIWS")
public class MIWS extends AbstractDataModel implements IRestConnection {

    private final StringProperty name;
    private final StringProperty host;
    private final IntegerProperty port;
    private final StringProperty login;
    private final StringProperty password;

    public static transient String[] listLabelID = {"mi.name", "mi.host", "mi.port", "mi.login", "mi.password"};

    public MIWS() {
        this("");

    }

    public MIWS(String name) {
        super("MIWS");
        this.name = new SimpleStringProperty("");
        this.host = new SimpleStringProperty("");
        this.port = new SimpleIntegerProperty();
        this.login = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
    }

    @Override
    public ArrayList<?> extractData() {
        ArrayList a = new ArrayList();
        a.add(name.get());
        a.add(host.get());
        a.add(port.get());
        a.add(login.get());
        a.add(password.get());
        return a;
    }

    @Override
    public void populateData(ArrayList<?> data) {
        if (data != null) {
            if (data.size() == 5) {
                name.set((String) data.get(0));
                host.set((String) data.get(1));
                port.set((Integer) data.get(2));
                login.set((String) data.get(3));
                password.set((String) data.get(4));
            }
        }
    }

    @Override
    public String toString() {
        if (!name.get().isEmpty()) {
            return name.get();
        }
        return getUrlConnection();
    }

    @Override
    public AbstractDataModel newInstance() {
        return new MIWS();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String ip) {
        this.name.set(ip);
    }

    public StringProperty getNameProperty() {
        return this.name;
    }

    public String getHost() {
        return host.get();
    }

    public void setHost(String ip) {
        this.host.set(ip);
    }

    public StringProperty getHostProperty() {
        return this.host;
    }

    public int getPort() {
        return port.get();
    }

    public void setPort(int port) {
        this.port.set(port);
    }

    public IntegerProperty getPortProperty() {
        return this.port;
    }

    @Override
    public String getLogin() {
        return login.get();
    }

    public void setLogin(String login) {
        this.login.set(login);
    }

    public StringProperty getLoginProperty() {
        return this.login;
    }

    @Override
    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty getPasswordProperty() {
        return this.password;
    }

    @Override
    public String getUrlConnection() {
        return "http://" + host.getValue() + ":" + port.getValue() + "/m3api-rest";
    }
}
