/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.amqp.amqp190;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class RabbitManagementTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitManagementTemplate.class);
    
    private String host;
    private int port;
    private String username;
    private String password;
    private WebResource resource;
    
    @PostConstruct
    public void initializeClient() {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(username, password));
        resource = client.resource(String.format("http://%s:%s", host,port));
    }
    
    public String getHost() {
        return host;
    }
    @Required
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    @Required
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getUsername() {
        return username;
    }
    @Required
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    @Required
    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<ConnectionInfo> getConnectionInfos() {
        // lets get the JSON as a String
        String connectionInfosRaw = resource.path("api").path("connections").accept("application/json").get(String.class);
        // unmarshall it
        return unmarshallConnectionInfos(connectionInfosRaw);
    }
    
    private List<ConnectionInfo> unmarshallConnectionInfos(String connectionInfosRaw) {
        List<ConnectionInfo> connectionInfos = new ArrayList<ConnectionInfo>();
        try {
            JSONArray jsonList = (JSONArray) new JSONParser().parse(connectionInfosRaw);
            for(Object object : jsonList) {
                JSONObject jsonObject = (JSONObject) object;
                ConnectionInfo info = new ConnectionInfo();
                info.setName((String) jsonObject.get("name"));
                info.setNumChannels((Long) jsonObject.get("channels"));
                connectionInfos.add(info);
            }
        } catch (ParseException exception) {
            LOGGER.error("Could not parse json " + connectionInfosRaw, exception);
        }
        return connectionInfos;
    }

    public ConnectionInfo getConnectionInfo(String hostAddr) {
        List<ConnectionInfo> infos = getConnectionInfos();
        for(ConnectionInfo info : infos) {
            if(info.getName().contains(hostAddr)) {
                return info;
            }
        }
        throw new NullPointerException("Could not find connectionInfo for hostAddr=" + hostAddr+"\nconnectionInfos=" + infos);
    }
}
