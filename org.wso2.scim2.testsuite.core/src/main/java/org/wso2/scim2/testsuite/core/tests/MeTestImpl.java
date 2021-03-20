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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;
import org.wso2.scim2.testsuite.core.httpclient.HTTPClient;
import org.wso2.scim2.testsuite.core.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;
import org.wso2.scim2.testsuite.core.tests.common.ResponseValidateTests;
import org.wso2.scim2.testsuite.core.tests.model.RequestPath;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.util.ArrayList;

/**
 * Implementation of Me test cases.
 */
public class MeTestImpl implements ResourceType {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;

    /**
     * Initialize.
     *
     * @param complianceTestMetaDataHolder
     */
    public MeTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
        url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.ME_ENDPOINT;
    }

    /**
     * Create test users.
     *
     * @return array of userIds
     * @throws ComplianceException
     * @throws GeneralComplianceException
     */
    private ArrayList<String> createTestsUsers(String noOfUsers) throws ComplianceException,
            GeneralComplianceException {

        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;

        ArrayList<String> definedUsers = new ArrayList<>();
        ArrayList<String> userIDs = new ArrayList<>();

        if (noOfUsers.equals("One")) {
            definedUsers.add(ComplianceConstants.DefinedInstances.defineUser);
        } else if (noOfUsers.equals("Many")) {
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser2);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser3);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser4);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser5);
        }

        HttpPost method = new HttpPost(url);
        // Create users.
        HttpClient client = HTTPClient.getHttpClient();
        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus = StringUtils.EMPTY;
        ArrayList<String> subTests = new ArrayList<>();
        for (int i = 0; i < definedUsers.size(); i++) {
            long startTime = System.currentTimeMillis();
            try {
                // Create Users.
                HttpEntity entity = new ByteArrayEntity(definedUsers.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    // Obtain the schema corresponding to the user.
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                    JSONDecoder jsonDecoder = new JSONDecoder();
                    User user = null;
                    try {
                        user = (User) jsonDecoder.decodeResource(responseString, schema, new User());
                    } catch (BadRequestException | CharonException | InternalErrorException e) {
                        long stopTime = System.currentTimeMillis();
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                                "Could not decode the server response of users create.",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
                    }
                    userIDs.add(user.getId());
                }
            } catch (Exception e) {
                // Read the response body.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                long stopTime = System.currentTimeMillis();
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                        "Could not create default users at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests), stopTime - startTime));
            }
        }
        return userIDs;
    }

    /**
     * This method cleans up the created used with the given id.
     *
     * @param id of a user and testcase name
     * @return boolean
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    private boolean cleanUpUser(String id, String testName) throws GeneralComplianceException, ComplianceException {

        long startTime = System.currentTimeMillis();
        String userEndpointURL = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;
        String deleteUserURL = userEndpointURL + "/" + id;
        HttpDelete method = new HttpDelete(deleteUserURL);
        HttpClient client = HTTPClient.getHttpClient();
        method = (HttpDelete) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus = StringUtils.EMPTY;
        ArrayList<String> subTests = new ArrayList<>();
        try {
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
            // Read the response body.
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            long stopTime = System.currentTimeMillis();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                            subTests), stopTime - startTime));
        }
        if (response.getStatusLine().getStatusCode() == 204) {
            return true;
        } else {
            long stopTime = System.currentTimeMillis();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                            subTests), stopTime - startTime));
        }
    }

    /**
     * Get me test case.
     *
     * @return array of test results
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults = new ArrayList<>();
        ArrayList<String> userID = null;
        // Create 1 test user.
        userID = createTestsUsers("One");
        String id = userID.get(0);
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Get Me");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?attributes=userName,name.givenName");
        requestPath2.setTestCaseName("Get Me with specific attributes");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?excludedAttributes=name.givenName,emails");
        requestPath3.setTestCaseName("Get Me with excluding attributes");

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            User user = null;
            String getMeURL = url + requestPaths[i].getUrl();
            HttpGet method = new HttpGet(getMeURL);
            HttpClient client = HTTPClient.getHttpClient();
            method = (HttpGet) HTTPClient.setAuthorizationHeader(
                    ComplianceConstants.DefinedInstances.defineUserName,
                    ComplianceConstants.DefinedInstances.defineUserPassword,
                    method);
            method.setHeader("Accept", "application/json");
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus = StringUtils.EMPTY;
            ArrayList<String> subTests = new ArrayList<>();
            String locationHeader = null;
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (response.getStatusLine().getStatusCode() != 501 &
                        response.getStatusLine().getStatusCode() != 308) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                    subTests.add("Expected : 200");
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not get the default user from url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                // Obtain the schema corresponding to user.
                // Unless configured returns core-user schema or else returns extended user schema).
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + id);
                    subTests.add("Status : Success");
                    subTests.add(StringUtils.EMPTY);
                } else {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + id);
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 308");
                subTests.add("Status : Permanent Redirect");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            }
        }
        // Clean the created user.
        cleanUpUser(id, "Get Me");
        return testResults;
    }

    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Post me test case.
     *
     * @return array of test results
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedUsers = new ArrayList<>();

        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedWithoutUserNameUser);

        ArrayList<String> userIDs = new ArrayList<>();
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Create Me");

//        RequestPath requestPath2 = new RequestPath();
//        requestPath2.setTestCaseName("Post Me with same userName");
//
//        RequestPath requestPath3 = new RequestPath();
//        requestPath3.setTestCaseName("Post Me without userName");

        requestPaths = new RequestPath[]{requestPath1};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            User user = null;
            HttpPost method = new HttpPost(url);
            method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            //create user test
            HttpClient client = HTTPClient.getHttpClient();
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus = StringUtils.EMPTY;
            ArrayList<String> subTests = new ArrayList<>();
            String locationHeader = null;
            try {
                // Create the user.
                HttpEntity entity = new ByteArrayEntity
                        (ComplianceConstants.DefinedInstances.defineUser.getBytes("UTF-8"));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (response.getStatusLine().getStatusCode() != 501 &
                        response.getStatusLine().getStatusCode() != 308) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                    subTests.add("Expected : 201");
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not create default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                }
            }
            if (response.getStatusLine().getStatusCode() == 201) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                // Obtain the schema corresponding to user.
                // Unless configured returns core-user schema or else returns extended user schema.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    try {
                        cleanUpUser(user.getId(), requestPaths[i].getTestCaseName());
                    } catch (GeneralComplianceException e1) {
                        long stopTime = System.currentTimeMillis();
                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Could not retrieve the user id",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
                    }
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + user.getId());
                    subTests.add("Status : Success");
                    subTests.add(StringUtils.EMPTY);
                } else {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + user.getId());
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    try {
                        cleanUpUser(user.getId(), requestPaths[i].getTestCaseName());
                    } catch (GeneralComplianceException e1) {
                        long stopTime = System.currentTimeMillis();
                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Could not retrieve the user id",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
                    }
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                }
                try {
                    cleanUpUser(user.getId(), requestPaths[i].getTestCaseName());
                } catch (GeneralComplianceException e1) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not retrieve the user id",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                }
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 308");
                subTests.add("Status : Permanent Redirect");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            }
        }
        return testResults;
    }

    /**
     * Patch me test case.
     *
     * @return array of test results
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedUsers = new ArrayList<>();

        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload2);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload3);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload4);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload5);

        ArrayList<String> userIDs = new ArrayList<>();
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Patch Me with add operation");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Patch Me with remove operation");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Patch Me with replace operation");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Patch Me with array of operations");

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setTestCaseName("Patch Me error validation");

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3, requestPath4, requestPath5};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            //create default user;
            ArrayList<String> userID;
            userID = createTestsUsers("One");
            String id = userID.get(0);
            User user = null;
            String patchUserURL = null;
            HttpPatch method = new HttpPatch(url);
            // Create user test.
            HttpClient client = HTTPClient.getHttpClient();
            method = (HttpPatch) HTTPClient.setAuthorizationHeader(
                    ComplianceConstants.DefinedInstances.defineUserName,
                    ComplianceConstants.DefinedInstances.defineUserPassword,
                    method);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus = StringUtils.EMPTY;
            ArrayList<String> subTests = new ArrayList<>();
            String locationHeader = null;
            try {
                // Patch the user.
                HttpEntity entity = new ByteArrayEntity
                        (definedUsers.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                method.setHeader("Accept", "application/json");
                method.setHeader("Content-Type", "application/json");
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                if (response.getStatusLine().getStatusCode() != 501 &
                        response.getStatusLine().getStatusCode() != 308 &
                        !requestPaths[i].getTestCaseName().equals("Patch Me error validation")) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                    subTests.add("Expected : 200");
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not patch the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                // Obtain the schema corresponding to user.
                // Unless configured returns core-user schema or else returns extended user schema).
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean the created user.
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + id);
                    subTests.add("Status : Success");
                    subTests.add(StringUtils.EMPTY);
                } else {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + id);
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    // Clean the created user.
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Patch Me error validation") &&
                    response.getStatusLine().getStatusCode() == 400) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 400");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 308");
                subTests.add("Status : Permanent Redirect");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus,
                                        subTests), stopTime - startTime));
            } else {
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            }
        }
        return testResults;
    }

    /**
     * Put me test case.
     *
     * @return array of test results
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedUsers = new ArrayList<>();

        definedUsers.add(ComplianceConstants.DefinedInstances.definedUpdatedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUpdatedUser2);
        ArrayList<String> userIDs = new ArrayList<>();
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Update Me");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Update Me with schema violation");

        requestPaths = new RequestPath[]{requestPath1, requestPath2};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            User user = null;
            ArrayList<String> userID = null;
            // Create 1 test user.
            userID = createTestsUsers("One");
            String id = userID.get(0);
            HttpPut method = new HttpPut(url);
            HttpClient client = HTTPClient.getHttpClient();
            method = (HttpPut) HTTPClient.setAuthorizationHeader(
                    ComplianceConstants.DefinedInstances.defineUserName,
                    ComplianceConstants.DefinedInstances.defineUserPassword,
                    method);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus = StringUtils.EMPTY;
            ArrayList<String> subTests = new ArrayList<>();
            String locationHeader = null;
            try {
                //update the user
                HttpEntity entity = new ByteArrayEntity
                        (definedUsers.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                method.setHeader("Accept", "application/json");
                method.setHeader("Content-Type", "application/json");
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                //get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                // Read the response body.
                //get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                if (response.getStatusLine().getStatusCode() != 501 &
                        response.getStatusLine().getStatusCode() != 308 &
                        !requestPaths[i].getTestCaseName().equals("Update Me with schema violation")) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                    subTests.add("Expected : 200");
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not update the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                //obtain the schema corresponding to user
                // unless configured returns core-user schema or else returns extended user schema)
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + id);
                    subTests.add("Status : Success");
                    subTests.add(StringUtils.EMPTY);
                } else {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.LOCATION_HEADER);
                    subTests.add("Actual : " + locationHeader);
                    subTests.add("Expected : " + url + "/" + id);
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 200");
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 308");
                subTests.add("Status : Permanent Redirect");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Update Me with " +
                    "schema violation") &&
                    response.getStatusLine().getStatusCode() == 400) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 400");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else {
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            }
        }
        return testResults;
    }

    /**
     * Delete me test case.
     *
     * @return array of test results
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> userIDs = new ArrayList<>();
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Delete user by ID");

//        RequestPath requestPath2 = new RequestPath();
//        requestPath2.setUrl(generateUniqueID());
//        requestPath2.setTestCaseName("User not found error response");

        requestPaths = new RequestPath[]{requestPath1};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            User user = null;
            ArrayList<String> userID = null;
            // Create 1 test user.
            userID = createTestsUsers("One");
            String id = userID.get(0);
            HttpDelete method = new HttpDelete(url);
            HttpClient client = HTTPClient.getHttpClient();
            method = (HttpDelete) HTTPClient.setAuthorizationHeader(
                    ComplianceConstants.DefinedInstances.defineUserName,
                    ComplianceConstants.DefinedInstances.defineUserPassword,
                    method);
            method.setHeader("Accept", "application/json");
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus = StringUtils.EMPTY;
            ArrayList<String> subTests = new ArrayList<>();
            try {
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
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                if (response.getStatusLine().getStatusCode() != 501 &
                        response.getStatusLine().getStatusCode() != 308 &
                        response.getStatusLine().getStatusCode() != 403) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                    subTests.add("Expected : 204");
                    subTests.add("Status : Failed");
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not delete the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == 204) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 204");
                subTests.add("Status : Success");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 204");
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 308");
                subTests.add("Status : Permanent Redirect");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
            } else if (response.getStatusLine().getStatusCode() == 403) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add("Actual : " + response.getStatusLine().getStatusCode());
                subTests.add("Expected : 204");
                subTests.add("Status : Failed");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to prohibit action giving 403",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
            } else {
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            }
        }
        return testResults;
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
