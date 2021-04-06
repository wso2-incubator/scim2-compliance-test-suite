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
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;
import org.wso2.scim2.testsuite.core.httpclient.HTTPClient;
import org.wso2.scim2.testsuite.core.objects.SCIMServiceProviderConfig;
import org.wso2.scim2.testsuite.core.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;
import org.wso2.scim2.testsuite.core.tests.common.ResponseValidateTests;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.util.ArrayList;

/**
 * Implementation of ServiceProviderConfig test cases.
 */
public class ServiceProviderConfigTestImpl implements ResourceType {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private SCIMServiceProviderConfig scimServiceProviderConfig = null;

    /**
     * Initializer.
     *
     * @param complianceTestMetaDataHolder Stores data required to run tests.
     */
    public ServiceProviderConfigTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
    }

    /**
     * Get serviceProviderConfig tests.
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
        Boolean errorOccured = false;
        // Construct the endpoint url.
        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.SERVICE_PROVIDER_ENDPOINT;
        // Specify the get request.
        HttpGet method = new HttpGet(url);
        HttpClient client = HTTPClient.getHttpClient();
        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus = StringUtils.EMPTY;
        ArrayList<String> subTests = new ArrayList<>();
        try {
            // Get the service provider configs.
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
                    (TestResult.ERROR, "Get ServiceProviderConfig",
                            "Could not get ServiceProviderConfig at url " + url,
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
            // Obtain the schema corresponding to serviceProviderConfig.
            SCIMResourceTypeSchema schema = complianceTestMetaDataHolder.getScimSchema().
                    getServiceProviderConfigSchema();
            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                scimServiceProviderConfig =
                        (SCIMServiceProviderConfig)
                                jsonDecoder.decodeResource(responseString, schema,
                                        new SCIMServiceProviderConfig());
                complianceTestMetaDataHolder.setScimServiceProviderConfig(scimServiceProviderConfig);
            } catch (BadRequestException | CharonException | InternalErrorException e) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, "Get ServiceProviderConfig",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests), stopTime - startTime));
                errorOccured = true;
            }
            try {
                ResponseValidateTests.runValidateTests(scimServiceProviderConfig, schema, null, null, method,
                        responseString, headerString.toString(), responseStatus, subTests);
            } catch (BadRequestException | CharonException e) {
                subTests.add("Status : Failed");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, "Get ServiceProviderConfig",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests), stopTime - startTime));
                errorOccured = true;
            } catch (GeneralComplianceException e) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, "Get ServiceProviderConfig",
                        e.getResult().getMessage(), ComplianceUtils.getWire(method,
                        responseString, headerString.toString(), responseStatus, subTests), stopTime - startTime));
                errorOccured = true;
            }
            if (!errorOccured) {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, "Get ServiceProviderConfig", StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            }
        } else {
            long stopTime = System.currentTimeMillis();
            testResults.add(new TestResult
                    (TestResult.ERROR, "Get ServiceProviderConfig",
                            StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString, headerString.toString(),
                            responseStatus, subTests), stopTime - startTime));
        }
        return testResults;
    }

    /**
     * Get serviceProviderConfig by id tests. This method is not valid for serviceProviderConfig according to the
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
     * Post serviceProviderConfig tests. This method is not valid for serviceProviderConfig according to the
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
     * Patch serviceProviderConfig tests. This method is not valid for serviceProviderConfig according to the
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
     * Put serviceProviderConfig tests. This method is not valid for serviceProviderConfig according to the
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
     * Delete serviceProviderConfig tests. This method is not valid for serviceProviderConfig according to the
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
     * Search serviceProviderConfig tests. This method is not valid for serviceProviderConfig according to the
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
