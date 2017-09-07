/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model.mi;

import com.kles.model.IRestConnection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author jchau
 */
public abstract class AbstractRestClientConnection implements IRestClient {

    protected final ObjectProperty<IRestConnection> environment;
    protected final ObjectProperty<AbstractDataMIModel> dataModel;

    public AbstractRestClientConnection() {
        environment = new SimpleObjectProperty<>();
        dataModel = new SimpleObjectProperty<>();
    }

    public ClientConfig buildClient() {
        ClientConfig clientConfig = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(environment.getValue().getLogin(), environment.getValue().getPassword());
        clientConfig.register(feature);
        clientConfig.register(JacksonFeature.class);
        return clientConfig;
    }

    public String buildMetadataExecute() {
        //return "http://" + environment.getValue().getHost() + ":" + environment.getValue().getPort() + "/m3api-rest/metadata/" + dataModel.getValue().getMIProgram();
        return environment.getValue().getUrlConnection() + "/metadata/" + dataModel.getValue().getMIProgram();
    }

    public String buildUrlExecute() {
        return environment.getValue().getUrlConnection() + "/execute/" + dataModel.getValue().getMIProgram() + "/" + dataModel.getValue().getMITransaction();
    }

    public ObjectProperty<IRestConnection> getEnvironmentProperty() {
        return environment;
    }

    public IRestConnection getEnvironment() {
        return environment.get();
    }

    public void setEnvironment(IRestConnection environment) {
        this.environment.set(environment);
    }

    public ObjectProperty<AbstractDataMIModel> getDataModelProperty() {
        return dataModel;
    }

    public AbstractDataMIModel getDataModel() {
        return dataModel.get();
    }

    public void setDataModel(AbstractDataMIModel dataModel) {
        this.dataModel.set(dataModel);
    }
}
