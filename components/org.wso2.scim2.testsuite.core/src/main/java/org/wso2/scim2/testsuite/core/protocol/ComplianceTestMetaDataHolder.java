/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.scim2.testsuite.core.protocol;

import org.wso2.scim2.testsuite.core.objects.SCIMResourceType;
import org.wso2.scim2.testsuite.core.objects.SCIMSchema;
import org.wso2.scim2.testsuite.core.objects.SCIMServiceProviderConfig;

/**
 * This contains the meta data that need to configure the test suite.
 */
public class ComplianceTestMetaDataHolder {

    private String url;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String authorizationServer;
    private String authorizationHeader;
    private String authorizationMethod;
    private static SCIMServiceProviderConfig scimServiceProviderConfig;
    private SCIMResourceType scimResourceType;
    private SCIMSchema scimSchema;

    public SCIMServiceProviderConfig getScimServiceProviderConfig() {

        return scimServiceProviderConfig;
    }

    public void setScimServiceProviderConfig(SCIMServiceProviderConfig scimServiceProviderConfig) {

        ComplianceTestMetaDataHolder.scimServiceProviderConfig = scimServiceProviderConfig;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getClient_id() {

        return clientId;
    }

    public void setClient_id(String clientId) {

        this.clientId = clientId;
    }

    public String getClient_secret() {

        return clientSecret;
    }

    public void setClient_secret(String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public String getAuthorization_server() {

        return authorizationServer;
    }

    public void setAuthorization_server(String authorizationServer) {

        this.authorizationServer = authorizationServer;
    }

    public String getAuthorization_header() {

        return authorizationHeader;
    }

    public void setAuthorization_header(String authorizationHeader) {

        this.authorizationHeader = authorizationHeader;
    }

    public String getAuthorization_method() {

        return authorizationMethod;
    }

    public void setAuthorization_method(String authorizationMethod) {

        this.authorizationMethod = authorizationMethod;
    }

    public void setScimResourceType(SCIMResourceType scimResourceType) {

        this.scimResourceType = scimResourceType;
    }

    public SCIMResourceType getScimResourceType() {

        return scimResourceType;
    }

    public SCIMSchema getScimSchema() {

        return scimSchema;
    }

    public void setScimSchema(SCIMSchema scimSchema) {

        this.scimSchema = scimSchema;
    }
}

