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
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;
import org.wso2.scim2.testsuite.core.httpclient.HTTPClient;
import org.wso2.scim2.testsuite.core.objects.SCIMResourceType;
import org.wso2.scim2.testsuite.core.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;
import org.wso2.scim2.testsuite.core.tests.common.ResponseValidateTests;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.util.ArrayList;

/**
 * Implementation of ResourceType test cases.
 */
public class ResourceTypeTestImpl implements ResourceType {

    private final ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    SCIMResourceType scimResourceType = null;

    public ResourceTypeTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
    }

    /**
     * Add assertion details.
     *
     * @param actual   Actual result.
     * @param status   Status of the assertion.
     * @param subTests Array containing assertions details.
     */
    private void addAssertion(int actual, String status, ArrayList<String> subTests) {

        subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
        subTests.add(ComplianceConstants.TestConstants.ACTUAL + actual);
        subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
        subTests.add(status);
        subTests.add(StringUtils.EMPTY);
    }

    /**
     * Get ResourceType tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        long startTime = System.currentTimeMillis();
        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        boolean errorOccurred = false;
        // Construct the endpoint url.
        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.RESOURCE_TYPE_ENDPOINT;
        // Specify the get request.
        HttpGet method = new HttpGet(url);
        HttpClient client = HTTPClient.getHttpClient();
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        try {
            // Get the resource types.
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
            assert response != null;
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            // Check for status returned.
            addAssertion(response.getStatusLine().getStatusCode(), ComplianceConstants.TestConstants.STATUS_FAILED,
                    subTests);
            long stopTime = System.currentTimeMillis();
            testResults.add(new TestResult
                    (TestResult.ERROR, ComplianceConstants.TestConstants.GET_RESOURCETYPE,
                            "Could not get ResourceType at url " + url,
                            ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests), stopTime - startTime));
            errorOccurred = true;
        }
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // Check for status returned.
            addAssertion(response.getStatusLine().getStatusCode(), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                    subTests);
            // Obtain the schema corresponding to resourceType.
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.
                    getInstance().getResourceTypeResourceSchema();
            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                scimResourceType =
                        jsonDecoder.decodeResource(responseString, schema,
                                new SCIMResourceType());
                complianceTestMetaDataHolder.setScimResourceType(scimResourceType);
            } catch (BadRequestException | CharonException | InternalErrorException e) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR,
                        ComplianceConstants.TestConstants.GET_RESOURCETYPE,
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests), stopTime - startTime));
                errorOccurred = true;
            }
            try {
                ResponseValidateTests.runValidateTests(scimResourceType, schema, null,
                        null, method,
                        responseString, headerString.toString(), responseStatus, subTests);
            } catch (BadRequestException | CharonException e) {
                subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR,
                        ComplianceConstants.TestConstants.GET_RESOURCETYPE,
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests), stopTime - startTime));
                errorOccurred = true;
            } catch (GeneralComplianceException e) {
                // Separately catching exception to get descriptive message from GeneralComplianceException.
                subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR,
                        ComplianceConstants.TestConstants.GET_RESOURCETYPE,
                        e.getResult().getMessage(), ComplianceUtils.getWire(method,
                        responseString, headerString.toString(), responseStatus, subTests), stopTime - startTime));
                errorOccurred = true;
            }
            if (!errorOccurred) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, ComplianceConstants.TestConstants.GET_RESOURCETYPE, StringUtils.EMPTY,
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(), responseStatus, subTests),
                                stopTime - startTime));
            }
        } else {
            long stopTime = System.currentTimeMillis();
            testResults.add(new TestResult
                    (TestResult.ERROR, ComplianceConstants.TestConstants.GET_RESOURCETYPE,
                            StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString, headerString.toString(),
                            responseStatus, subTests), stopTime - startTime));
        }
        return testResults;
    }

    /**
     * Get ResourceType by id tests. This method is not valid for ResourceType according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-4
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Post ResourceType tests. This method is not valid for ResourceType according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-4
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Patch ResourceType tests. This method is not valid for ResourceType according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-4
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Put ResourceType tests. This method is not valid for ResourceType according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-4
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Delete ResourceType tests. This method is not valid for ResourceType according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-4
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Search ResourceType tests. This method is not valid for ResourceType according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-4
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> searchMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Execute all tests.
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> executeAllTests() throws GeneralComplianceException, ComplianceException {

        // This method is not needed for the current implementation.
        return null;
    }
}
