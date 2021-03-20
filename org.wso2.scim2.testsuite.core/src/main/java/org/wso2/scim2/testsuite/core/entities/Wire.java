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
package org.wso2.scim2.testsuite.core.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Wire {

    public static final Wire EMPTY = new Wire("<empty>", "<empty>", "<empty>", "<empty>", "<empty>", "<empty>",
            "<empty>", "<empty>", "<empty>", "<empty>");

    @XmlElement(name = "to_server")
    String toServer = "";

    @XmlElement(name = "from_server")
    String fromServer = "";

    @XmlElement(name = "tests")
    String tests = "";

    @XmlElement(name = "responseBody")
    String responseBody = "";

    @XmlElement(name = "responseHeaders")
    String responseHeaders = "";

    @XmlElement(name = "responseStatus")
    String responseStatus = "";

    @XmlElement(name = "requestType")
    String requestType = "";

    @XmlElement(name = "requestUri")
    String requestUri = "";

    @XmlElement(name = "requestBody")
    String requestBody = "";

    @XmlElement(name = "requestHeaders")
    String requestHeaders = "";

    public Wire() {

    }

    public Wire(String toServer, String fromServer, String tests, String responseBody, String responseHeaders,
                String responseStatus, String requestType, String requestUri, String requestBody,
                String requestHeaders) {

        this.toServer = toServer;
        this.fromServer = fromServer;
        this.tests = tests;
        this.responseBody = responseBody;
        this.responseHeaders = responseHeaders;
        this.responseStatus = responseStatus;
        this.requestType = requestType;
        this.requestUri = requestUri;
        this.requestBody = requestBody;
        this.requestHeaders = requestHeaders;
    }

    public String getToServer() {

        return toServer;
    }

    public String getFromServer() {

        return fromServer;
    }

    public String getTests() {

        return tests;
    }

    public String getResponseBody() {

        return responseBody;
    }

    public String getResponseHeaders() {

        return responseHeaders;
    }

    public String getResponseStatus() {

        return responseStatus;
    }

    public String getRequestType() {

        return requestType;
    }

    public String getRequestUri() {

        return requestUri;
    }

    public String getRequestBody() {

        return requestBody;
    }

    public String getRequestHeaders() {

        return requestHeaders;
    }
}
