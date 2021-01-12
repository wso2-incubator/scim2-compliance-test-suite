/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.scim2.compliance.tests;

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
import org.wso2.scim2.compliance.entities.TestResult;
import org.wso2.scim2.compliance.exception.ComplianceException;
import org.wso2.scim2.compliance.exception.GeneralComplianceException;
import org.wso2.scim2.compliance.httpclient.HTTPClient;
import org.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.compliance.protocol.ComplianceUtils;
import org.wso2.scim2.compliance.tests.common.ResponseValidateTests;
import org.wso2.scim2.compliance.tests.model.RequestPath;
import org.wso2.scim2.compliance.utils.ComplianceConstants;

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
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                                "Could not decode the server response of users create.",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
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
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                        "Could not create default users at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests)));
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
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                            subTests)));
        }
        if (response.getStatusLine().getStatusCode() == 204) {
            return true;
        } else {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                            subTests)));
        }
    }

    /**
     * Get me test case.
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
                if (response.getStatusLine().getStatusCode() != 501 &
                        response.getStatusLine().getStatusCode() != 308) {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not get the default user from url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                // Obtain the schema corresponding to user.
                // Unless configured returns core-user schema or else returns extended user schema).
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
            } else {
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
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
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not create default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                }
            }
            if (response.getStatusLine().getStatusCode() == 201) {
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
                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Could not retrieve the user id",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
                    }
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {
                    try {
                        cleanUpUser(user.getId(), requestPaths[i].getTestCaseName());
                    } catch (GeneralComplianceException e1) {
                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Could not retrieve the user id",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
                    }
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                }
                try {
                    cleanUpUser(user.getId(), requestPaths[i].getTestCaseName());
                } catch (GeneralComplianceException e1) {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not retrieve the user id",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                }
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
            } else {
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
            }
        }
        return testResults;
    }

    /**
     * Patch me test case.
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
                        response.getStatusLine().getStatusCode() != 308 & requestPaths[i].getTestCaseName() != "Patch" +
                        " Me error validation") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not patch the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                // Obtain the schema corresponding to user.
                // Unless configured returns core-user schema or else returns extended user schema).
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean the created user.
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    // Clean the created user.
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "Patch Me error validation" &&
                    response.getStatusLine().getStatusCode() == 400) {
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus,
                                        subTests)));
            } else {
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
            }
        }
        return testResults;
    }

    /**
     * Put me test case.
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
                        requestPaths[i].getTestCaseName() != "Update Me with schema violation") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not update the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                //obtain the schema corresponding to user
                // unless configured returns core-user schema or else returns extended user schema)
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString.toString(), responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
            } else if (requestPaths[i].getTestCaseName() == "Update Me with " +
                    "schema violation" &&
                    response.getStatusLine().getStatusCode() == 400) {
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else {
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
            }
        }
        return testResults;
    }

    /**
     * Delete me test case.
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
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not delete the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
            }

            if (response.getStatusLine().getStatusCode() == 204) {
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 501) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else if (response.getStatusLine().getStatusCode() == 308) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to redirect the client using HTTP status code 308 ",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
            } else if (response.getStatusLine().getStatusCode() == 403) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "service provider choose to prohibit action giving 403",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
            } else {
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests)));
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
