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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.wso2.scim2.testsuite.core.entities.Wire;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;

import java.util.ArrayList;

/**
 * This class contains utils used in the test suite.
 */
public class ComplianceUtils {

    /**
     * method to get the wire.
     *
     * @param method         Http method.
     * @param responseBody   Json response form service provider.
     * @param headerString   Headers from service provider.
     * @param responseStatus Status code of the response.
     * @param subTests       Assertions done for each test case.
     * @return wire Object which contains all the test data.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    public static Wire getWire(HttpRequestBase method, String responseBody,
                               String headerString, String responseStatus,
                               ArrayList<String> subTests) throws ComplianceException {

        StringBuffer toServer = new StringBuffer();
        StringBuffer fromServer = new StringBuffer();
        StringBuffer subTestsPerformed = new StringBuffer();
        StringBuffer requestUri = new StringBuffer();
        StringBuffer requestType = new StringBuffer();
        StringBuffer requestHeaders = new StringBuffer();
        StringBuffer requestBody = new StringBuffer();

        toServer.append(method.getRequestLine().getMethod()).append(" ");
        requestType.append(method.getRequestLine().getMethod());
        toServer.append(method.getRequestLine().getUri() + "\n");
        requestUri.append(method.getRequestLine().getUri());
        toServer.append(method.getRequestLine().getProtocolVersion().getProtocol());
        for (org.apache.http.Header header : method.getAllHeaders()) {
            toServer.append(header.getName()).append(": ").append(header.getValue()).append("\n");
            requestHeaders.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        toServer.append("\n");
        if (method.getMethod() != "GET" && method.getMethod() != "DELETE") {
            try {
                HttpEntity entity = ((HttpEntityEnclosingRequest) method).getEntity();
                toServer.append(EntityUtils.toString(entity));
                requestBody.append(EntityUtils.toString(entity));
            } catch (Exception e) {
                throw new ComplianceException(500, "Error in getting the request payload");
            }
        }
        fromServer.append("\n" + "Headers : " + "\n");
        fromServer.append(headerString + "\n");
        fromServer.append("\n" + "Status : ");
        fromServer.append(responseStatus + "\n");
        fromServer.append("\n" + responseBody);
        for (String subTest : subTests) {
            subTestsPerformed.append(subTest).append("\n");
        }
        return new Wire(toServer.toString(), fromServer.toString(), subTestsPerformed.toString(), responseBody,
                headerString, responseStatus, requestType.toString(), requestUri.toString(), requestBody.toString(),
                requestHeaders.toString());
    }

    public static Wire getWire(Throwable e) {

        return new Wire(ExceptionUtils.getFullStackTrace(e), "", "", "", "", "", "", "", "", "");
    }
}
