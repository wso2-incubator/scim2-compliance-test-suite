/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.wso2.scim2.compliance.httpclient;

import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.Charset;

/**
 * This class is to depicts the HTTP client.
 */
public class HTTPClient {

    private static HttpClient httpClient = null;

    public static HttpClient getHttpClient() {
        if(httpClient == null) {
            HttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
            return httpClient;
        }
        return httpClient;
    }

    public static HttpRequestBase setAuthorizationHeader (ComplianceTestMetaDataHolder complianceTestMetaDataHolder,
                                                          HttpRequestBase method) {

        String auth = complianceTestMetaDataHolder.getUsername() + ":" + complianceTestMetaDataHolder.getPassword();
        if (!auth.equals(":")) {
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            method.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return method;
    }

    public static HttpRequestBase setAuthorizationHeader (String userName, String password,
                                                          HttpRequestBase method) {

        String auth = userName + ":" + password;
        if (!auth.equals(":")) {
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            method.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return method;
    }

}


