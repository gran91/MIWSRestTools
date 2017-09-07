/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model.mi;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author jchau
 */
public class DefaultRestClientMetadataConnection extends AbstractRestClientConnection implements IRestClient {

    public static int LIST_API = 1;
    public static int LIST_TRANS = 2;
    public static int EXCUTE_API = 3;
    public static int CLEAR_CACHE = 4;
    public int action = LIST_API;
    private int maxRecord = 0;

    public DefaultRestClientMetadataConnection() {
        super();
        dataModel.setValue(new GenericDataMIModel());
    }

    @Override
    public String buildMetadataExecute() {
        return environment.getValue().getUrlConnection() + "/metadata/" + dataModel.getValue().getMIProgram();
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    @Override
    public WebTarget getWebTarget() {
        Client client = ClientBuilder.newClient(buildClient());
        WebTarget webTarget = null;
        switch (action) {
            case 1:
                dataModel.getValue().setMIProgram("");
                webTarget = client.target(buildMetadataExecute());
                break;
            case 2:
                webTarget = client.target(buildMetadataExecute());
                break;
            case 3:
                webTarget = buildExecuteTarget(client);
                break;
            case 4:
                webTarget = client.target(buildClearCache(client));
                break;
        }
        return webTarget;
    }

    @Override
    public String buildUrlExecute() {
        return environment.getValue().getUrlConnection() + "/execute/" + dataModel.getValue().getMIProgram() + "/" + dataModel.getValue().getMITransaction() + ";maxrecs=" + maxRecord;
    }

    private WebTarget buildExecuteTarget(Client client) {
        WebTarget webTarget = client.target(buildUrlExecute());
        if (((GenericDataMIModel) (dataModel.getValue())).getInputData() != null) {
            for (Map.Entry<String, String> t : ((GenericDataMIModel) (dataModel.getValue())).getInputData().entrySet()) {
                webTarget = webTarget.queryParam(t.getKey(), t.getValue());
            }
        }
        return webTarget;
    }

    public String buildClearCache(Client client) {
        return environment.getValue().getUrlConnection() + "/metadata/clearcache/" + dataModel.getValue().getMIProgram();
    }

    public int getMaxRecord() {
        return maxRecord;
    }

    public void setMaxRecord(int maxRecord) {
        this.maxRecord = maxRecord;
    }

}
