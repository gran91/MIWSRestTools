/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.model.mi;

import javax.ws.rs.client.WebTarget;

/**
 *
 * @author JCHAUT
 */
public interface IRestClient {

    public WebTarget getWebTarget();
}
