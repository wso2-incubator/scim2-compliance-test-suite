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

package org.wso2.scim2.testsuite.core.tests;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.CriticalComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;
import org.wso2.scim2.testsuite.core.httpclient.HTTPClient;
import org.wso2.scim2.testsuite.core.objects.SCIMSchema;
import org.wso2.scim2.testsuite.core.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;
import org.wso2.scim2.testsuite.core.utils.SchemaBuilder;

import java.util.ArrayList;

/**
 * Implementation of Schema test cases.
 */
public class SchemaTestImpl implements ResourceType {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private SCIMSchema scimSchema = new SCIMSchema();

    /**
     * Initializer.
     *
     * @param complianceTestMetaDataHolder
     */
    public SchemaTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
    }

    /**
     * Method to get the schemas.
     *
     * @return
     * @throws
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        long startTime = System.currentTimeMillis();
        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        // Set the scim schema object.
        complianceTestMetaDataHolder.setScimSchema(scimSchema);
        Boolean errorOccured = false;
        // Construct the endpoint url.
        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.SCHEMAS_ENDPOINT;
        // Specify the get request.
        HttpGet method = new HttpGet(url);
        HttpClient client = HTTPClient.getHttpClient();
        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus = StringUtils.EMPTY;
        ArrayList<String> subTests = new ArrayList<>();
        try {
            // Get the schemas.
            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
        } catch (Exception e) {
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            // Check for status returned.
            subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
            subTests.add("Actual : " + response.getStatusLine().getStatusCode());
            subTests.add("Expected : 200");
            subTests.add("Status : Failed");
            subTests.add(StringUtils.EMPTY);
            long stopTime = System.currentTimeMillis();
            testResults.add(new TestResult
                    (TestResult.ERROR, "Get Schemas",
                            "Could not get Schemas at url " + url,
                            ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests), stopTime - startTime));
            errorOccured = true;
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            // Check for status returned.
            subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
            subTests.add("Actual : " + response.getStatusLine().getStatusCode());
            subTests.add("Expected : 200");
            subTests.add("Status : Success");
            subTests.add(StringUtils.EMPTY);
            // Build the schemas according to service provider.
            try {
                SchemaBuilder.buildSchema(responseString, method, headerString.toString(), responseStatus,
                        subTests, url, scimSchema);
            } catch (CriticalComplianceException e) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR,
                        "Get Schemas",
                        e.getResult().getMessage(), ComplianceUtils.getWire(method,
                        responseString, headerString.toString(), responseStatus, subTests), stopTime - startTime));
                errorOccured = true;
            }
            if (!errorOccured) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, "Get Schemas", StringUtils.EMPTY, ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                                stopTime - startTime));
            }
        } else {
            long stopTime = System.currentTimeMillis();
            testResults.add(new TestResult
                    (TestResult.ERROR, "Get Schemas",
                            StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString, headerString.toString(),
                            responseStatus, subTests), stopTime - startTime));
        }
        return testResults;
    }

    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> searchMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> executeAllTests() throws GeneralComplianceException, ComplianceException {

        return null;
    }
}
