/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.task.rest;

import com.kles.model.mi.IRestClient;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author JCHAUT
 */
public class RestGetTask extends Task<String> {

    private IRestClient restClient;
    private String mediaType = MediaType.APPLICATION_JSON;
    private String method = "GET";
    public static String GET = "GET";
    public static String POST = "POST";
    public static ResourceBundle resourceMessage = ResourceBundle.getBundle("resources/miresttools", Locale.getDefault());

    public RestGetTask(IRestClient rest, String mediatype) {
        this.restClient = rest;
        this.mediaType = mediatype;
    }

    public RestGetTask(IRestClient rest) {
        restClient = rest;
    }

    @Override
    protected String call() throws Exception {
        try {
            updateMessage(resourceMessage.getString("message.request"));
            WebTarget target = restClient.getWebTarget();
            updateMessage(resourceMessage.getString("message.connection"));
            if (isCancelled()) {
                return "";
            }
            Invocation.Builder invocationBuilder = target.request(this.mediaType);
            if (isCancelled()) {
                return "";
            }
            Response response = (method.equals(GET)) ? invocationBuilder.get() : invocationBuilder.post(Entity.json(""));
            if (isCancelled()) {
                return "";
            }
            System.out.println(response.getStatus());
            System.out.println(response.getStatusInfo());
            if (response.getStatus() == 200 || (response.getStatus() == 204 && method.equals(POST))) {
                updateMessage(resourceMessage.getString("message.connectionOK"));
                return response.readEntity(String.class);
            } else {
                updateMessage(String.format(resourceMessage.getString("message.connectionKO"), new String[]{response.getStatusInfo() + " (" + response.getStatus() + ")"}));
                throw new RuntimeException(response.getStatusInfo() + " (" + response.getStatus() + ")");
            }
        } catch (Exception e) {
            updateMessage(String.format(resourceMessage.getString("message.connectionKO"), new String[]{e.getLocalizedMessage()}));
            throw new RuntimeException(e);
        }
    }

    public IRestClient getRestClient() {
        return restClient;
    }

    public void setRestClient(IRestClient restClient) {
        this.restClient = restClient;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
