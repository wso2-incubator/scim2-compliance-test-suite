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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.UUID;

/**
 * Implementation of  for User test cases.
 */
public class UserTestImpl implements ResourceType {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;
    private RequestPath[] requestPaths;
    private ArrayList<String> userIDs = new ArrayList<>();
    //private TestResult[] testResults;
    private ArrayList<TestResult> testResults = new ArrayList<>();

    /**
     * Initialize.
     *
     * @param complianceTestMetaDataHolder
     */
    public UserTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;

    }

    /**
     * Create test users.
     *
     * @return
     * @throws ComplianceException
     * @throws GeneralComplianceException
     */
    private ArrayList<String> createTestsUsers() throws ComplianceException, GeneralComplianceException {

        ArrayList<String> definedUsers = new ArrayList<>();
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser2);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser3);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser4);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser5);

        HttpPost method = new HttpPost(url);
        // Create users.
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
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
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                        subTests)));
                    }
                    userIDs.add(user.getId());
                }
            } catch (Exception e) {
                // Read the response body.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                        "Could not create default users at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        }
        return userIDs;
    }

    /**
     * This method creates a user and return its id.
     *
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    private String initiateUser(String testName) throws GeneralComplianceException, ComplianceException {

        User user = null;

        HttpPost method = new HttpPost(url);
        // Create user test.
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests = new ArrayList<>();
        try {
            // Create the user.
            HttpEntity entity = new ByteArrayEntity
                    (ComplianceConstants.DefinedInstances.DEFINED_USER.getBytes("UTF-8"));
            method.setEntity(entity);
            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " " +
                    response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            // Read the response body.
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not create default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 201) {
            // Obtain the schema corresponding to user.
            // Unless configured returns core-user schema or else returns extended user schema.
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "Response validation error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                return user.getId();
            } catch (CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "User id retrieval error occurred.",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        } else {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Initiating User caused an error.",
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
    }

    /**
     * This method cleans up the created used with the given id.
     *
     * @param id
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    public boolean cleanUpUser(String id, String testName) throws GeneralComplianceException, ComplianceException {

        String deleteUserURL = url + "/" + id;

        HttpDelete method = new HttpDelete(deleteUserURL);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpDelete) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests = new ArrayList<>();
        try {

            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            // Read the response body.
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
        if (response.getStatusLine().getStatusCode() == 204) {
            return true;
        } else {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
    }

    /**
     * Method check whether return users are sorted or not.
     *
     * @param userList
     * @return
     * @throws CharonException
     */
    private boolean isUserListSorted(ArrayList<User> userList) throws CharonException {

        boolean sorted = true;
        for (int i = 1; i < userList.size(); i++) {
            if (userList.get(i - 1).getId().compareTo(userList.get(i).getId()) > 0) {
                sorted = false;
            }
        }
        return sorted;
    }

    private static String generateUniqueID() {

        return UUID.randomUUID().toString();
    }

    private void initiateData() throws ComplianceException, GeneralComplianceException {
        // Initialize 5 users.
        createTestsUsers();

        // Creating objects to store sub test information.
        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl("/");
        requestPath1.setTestCaseName("List Users");

//        RequestPath requestPath2 = new RequestPath();
//        requestPath2.setUrl("/" + userIDs.get(0));
//        requestPath2.setTestCaseName("Get user by ID");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?filter=userName+eq+loginUser1");
        requestPath3.setTestCaseName("Get user with Filter");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl("?startIndex=1&count=2");
        requestPath4.setTestCaseName("Get users with Pagination");

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setUrl("?sortBy=id&sortOrder=ascending");
        requestPath5.setTestCaseName("Sort test");

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setUrl("?filter=userName+eq+loginUser1&startIndex=1&count=1");
        requestPath6.setTestCaseName("Filter with pagination test");

        RequestPath requestPath7 = new RequestPath();
        requestPath7.setUrl("?startIndex=-1&count=2");
        requestPath7.setTestCaseName("Get users having negative number as index");

        RequestPath requestPath8 = new RequestPath();
        requestPath8.setUrl("?count=2");
        requestPath8.setTestCaseName("Get users without index and only using count");

        RequestPath requestPath9 = new RequestPath();
        requestPath9.setUrl("?attributes=userName,name.givenName");
        requestPath9.setTestCaseName("Get users with specific attributes");

        RequestPath requestPath10 = new RequestPath();
        requestPath10.setUrl("?excludedAttributes=name.givenName,emails");
        requestPath10.setTestCaseName("Get users with excluding attributes");

//        RequestPath requestPath11 = new RequestPath();
//        requestPath11.setUrl(String.format("/%s/?attributes=userName,name.givenName", userIDs.get(0)));
//        requestPath11.setTestCaseName("Get a user with specific attributes");
//
//        RequestPath requestPath12 = new RequestPath();
//        requestPath12.setUrl(String.format("/%s/?excludedAttributes=name.givenName,emails", userIDs.get(0)));
//        requestPath12.setTestCaseName("Get a user with excluding attributes");

        // This array hold the sub tests details.
        requestPaths = new RequestPath[]{requestPath1, requestPath3, requestPath4, requestPath5, requestPath6,
                requestPath7, requestPath8, requestPath9, requestPath10};

    }

    /**
     * Get user test.
     *
     * @return array of TestResult
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        // Initiate data necessary for getMethod test.
        initiateData();

        for (int i = 0; i < requestPaths.length; i++) {

            String requestUrl = url + requestPaths[i].getUrl();
            HttpGet method = new HttpGet(requestUrl);

            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
            Integer startIndex = null;
            Integer count = null;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                        "Could not list the users at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));

                continue;
            }

            if (response.getStatusLine().getStatusCode() == 200) {

                // Obtain the schema corresponding to user.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                ArrayList<User> userList = new ArrayList<>();
                JSONObject jsonObjResponse = null;
                User user = null;
                try {
                    // Called only for user get by id.

                    JSONObject jsonObj = new JSONObject(responseString);
                    jsonObjResponse = jsonObj;
                    JSONArray usersArray = jsonObj.getJSONArray("Resources");
                    startIndex = (Integer) jsonObjResponse.get("startIndex");
                    count = (Integer) jsonObjResponse.get("totalResults");
                    JSONObject tmp;

                    for (int j = 0; j < usersArray.length(); j++) {
                        tmp = usersArray.getJSONObject(j);
                        userList.add((User) jsonDecoder.decodeResource(tmp.toString(), schema, new User()));
                        try {
                            ResponseValidateTests.runValidateTests(userList.get(j), schema,
                                    null, null, method,
                                    responseString, headerString, responseStatus, subTests);

                        } catch (BadRequestException | CharonException e) {

                            testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Response Validation Error",
                                    ComplianceUtils.getWire(method, responseString, headerString,
                                            responseStatus, subTests)));
                            continue;
                        }
                    }

                } catch (JSONException e) {

//                    throw new ComplianceException(500, "Error in decoding the returned list resource.");
                    continue;
                } catch (BadRequestException | CharonException | InternalErrorException e) {

                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    continue;
                }

                // Check for all created groups.
                if (requestPaths[i].getTestCaseName() == "List Users") {
                    try {
                        //check for list of users returned
                        subTests.add(ComplianceConstants.TestConstants.ALL_USERS_IN_TEST);
                        ArrayList<String> returnedUserIDs = new ArrayList<>();
                        for (User u : userList) {
                            returnedUserIDs.add(u.getId());
                        }
                        for (String id : userIDs) {
                            if (!returnedUserIDs.contains(id)) {

                                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                        "Response does not contain all the created users",
                                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                                subTests)));
                                continue;
                            }
                        }
                    } catch (CharonException e) {

//                    throw new ComplianceException(500, "Could not get the created user id");
                        continue;
                    }
                } else if (requestPaths[i].getTestCaseName() == "Get user with Filter") {

                    subTests.add(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST);
                    String value = "loginUser1";
                    for (User user1 : userList) {
                        try {
                            if (!value.equals(user1.getUserName())) {

                                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                        "Response does not contain the expected users",
                                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                                subTests)));
                                continue;
                            }
                        } catch (CharonException e) {
                            continue;
                        }
                    }
                } else if (requestPaths[i].getTestCaseName() == "Get users with Pagination" ||
                        requestPaths[i].getTestCaseName() == "Get users having negative number as index") {
                    subTests.add(ComplianceConstants.TestConstants.PAGINATION_USER_TEST);

                    if (userList.size() != 2) {

                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Response does not contain right number of pagination.",
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                        subTests)));
                        continue;
                    }

                } else if (requestPaths[i].getTestCaseName() == "Sort test") {
                    subTests.add(ComplianceConstants.TestConstants.SORT_USERS_TEST);
                    try {
                        if (isUserListSorted(userList)) {

                            testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Response does not contain the sorted list of users",
                                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                            subTests)));
                        }
                    } catch (CharonException e) {
                        continue;
                    }
                } else if (requestPaths[i].getTestCaseName() == "Filter with pagination test") {

                    subTests.add(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION);

                    if (userList.size() != 1) {

                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Response does not contain right number of users.",
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                        subTests)));
                        continue;
                    }

                    String value = "loginUser1";
                    for (User user1 : userList) {
                        try {
                            if (!value.equals(user1.getUserName())) {

                                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                        "Response does not contain the expected users",
                                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                                subTests)));
                                continue;
                            }
                        } catch (CharonException e) {
                            continue;
                        }
                    }

                } else if (requestPaths[i].getTestCaseName() == "Get users without index and only using count") {
                    subTests.add(ComplianceConstants.TestConstants.PAGINATION_USER_TEST);

                    if (startIndex != 1 && count != 2) {

                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Response does not contain right number of pagination.",
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                        subTests)));
                        continue;
                    }

                }

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString,
                                headerString, responseStatus, subTests)));
            } else {

                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString,
                                headerString, responseStatus, subTests)));
            }
        }

        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "get users test");
        }

        // This should be a array containing results.
        return testResults;
    }

    /**
     * get user by Id test.
     *
     * @return array of TestResult
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> userIDs = new ArrayList<>();
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Get user by ID");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?attributes=userName,name.givenName");
        requestPath2.setTestCaseName("Get a user with specific attributes");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?excludedAttributes=name.givenName,emails");
        requestPath3.setTestCaseName("Get a user with excluding attributes");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl(generateUniqueID());
        requestPath4.setTestCaseName("User not found error response");

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3, requestPath4};

        for (int i = 0; i < requestPaths.length; i++) {        //create default user;
            String id = initiateUser("Get User");
            User user = null;
            String getUserURL = url + "/" + id + requestPaths[i].getUrl();

            HttpGet method = new HttpGet(getUserURL);

            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
            ArrayList<String> subTests = new ArrayList<>();
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean the created user.
                cleanUpUser(id, "Get User");
                if (requestPaths[i].getTestCaseName() != "User not found error response") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not get the default user from url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
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
                    cleanUpUser(id, "Get User");
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString, responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {
                    // Clean the created user.
                    cleanUpUser(id, "Get User");
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    continue;

                }

                // Clean the created user.
                cleanUpUser(id, "Get User");
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "User not found error response" &&
                    response.getStatusLine().getStatusCode() == 404) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));

            } else {
                // Clean the created user.
                cleanUpUser(id, "Get User");
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            }
        }
        return testResults;
    }

    /**
     * Post user test.
     *
     * @return array of TestResult
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
        requestPath1.setTestCaseName("Post User");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Post User with same userName");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Post User without userName");

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3};

        for (int i = 0; i < requestPaths.length; i++) {
            User user = null;
            HttpPost method = new HttpPost(url);
            //create user test
            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
            ArrayList<String> subTests = new ArrayList<>();
            try {
                // Create the user.
                HttpEntity entity = new ByteArrayEntity
                        (definedUsers.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPaths[i].getTestCaseName() == "Post User") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not create default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            }

            if (response.getStatusLine().getStatusCode() == 201) {
                // Obtain the schema corresponding to user.
                // Unless configured returns core-user schema or else returns extended user schema.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = (User) jsonDecoder.decodeResource(responseString, schema, new User());
                    userIDs.add(user.getId());

                } catch (BadRequestException | CharonException | InternalErrorException e) {

                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null, null, method,
                            responseString, headerString, responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {

                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString,
                                headerString, responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "Post User with same userName" &&
                    response.getStatusLine().getStatusCode() == 409) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Server successfully given the expected error 409(conflict) message",
                                ComplianceUtils.getWire(method, responseString,
                                        headerString, responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "Post User without userName" &&
                    response.getStatusLine().getStatusCode() == 400) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Server successfully given the expected error 404(Required attribute userName is " +
                                        "missing in the SCIM Object) message",
                                ComplianceUtils.getWire(method, responseString,
                                        headerString, responseStatus, subTests)));
            } else {
                testResults.add(
                        new TestResult
                                (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                        "", ComplianceUtils.getWire(method, responseString,
                                        headerString, responseStatus, subTests)));
            }
        }

        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "Post users test");
        }

        return testResults;
    }

    /**
     * Patch user test.
     *
     * @return array of TestResult
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
        requestPath1.setTestCaseName("Patch User with add operation");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Patch User with remove operation");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Patch User with replace operation");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Patch User with array of operations");

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setTestCaseName("Patch User error validation");

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3, requestPath4, requestPath5};

        for (int i = 0; i < requestPaths.length; i++) {
            String id = initiateUser("Patch User");
            User user = null;
            String patchUserURL = null;
            patchUserURL = url + "/" + id;

            HttpPatch method = new HttpPatch(patchUserURL);
            // Create user test.
            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpPatch) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
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
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean the created user.
                cleanUpUser(id, "Patch User");
                if (requestPaths[i].getTestCaseName() != "Patch User error validation") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not patch the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
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
                    cleanUpUser(id, "Patch User");
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString, responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {
                    // Clean the created user.
                    cleanUpUser(id, "Patch User");
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    continue;
                }
                // Clean the created user.
                cleanUpUser(id, "Patch User");
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "Patch User error validation" &&
                    response.getStatusLine().getStatusCode() == 400) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString,
                                        responseStatus, subTests)));

            } else {
                // Clean the created user.
                cleanUpUser(id, "Patch User");
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            }
        }
        return testResults;
    }

    /**
     * Put user test.
     *
     * @return array of TestResult
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
        requestPath1.setTestCaseName("Update User");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Update user with schema violation");

        requestPaths = new RequestPath[]{requestPath1, requestPath2};

        for (int i = 0; i < requestPaths.length; i++) {

            String id = initiateUser("Update User");
            User user = null;
            String updateUserURL = null;
            updateUserURL = url + "/" + id;

            HttpPut method = new HttpPut(updateUserURL);

            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpPut) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
            ArrayList<String> subTests = new ArrayList<>();
            try {
                // Update the user.
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
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean the created user.
                cleanUpUser(id, "Update User");
                if (requestPaths[i].getTestCaseName() != "Update user with schema violation") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not update the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
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
                    //clean the created user
                    cleanUpUser(id, "Update User");
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null,
                            null, method,
                            responseString, headerString, responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {
                    // Clean the created user.
                    cleanUpUser(id, "Update User");
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                // Clean the created user.
                cleanUpUser(id, "Update User");
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "Update user with schema violation" &&
                    response.getStatusLine().getStatusCode() == 400) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString,
                                        responseStatus, subTests)));

            } else {
                // Clean the created user.
                cleanUpUser(id, "Update User");
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            }
        }
        return testResults;
    }

    /**
     * Delete user test.
     *
     * @return array of TestResult
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
        requestPath1.setUrl("");
        requestPath1.setTestCaseName("Delete user by ID");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("23");
        requestPath2.setTestCaseName("User not found error response");

        requestPaths = new RequestPath[]{requestPath1, requestPath2};

        for (int i = 0; i < requestPaths.length; i++) {
            String id = initiateUser("Delete User");
            User user = null;
            String deleteUserURL = null;
            deleteUserURL = url + "/" + id + requestPaths[i].getUrl();

            HttpDelete method = new HttpDelete(deleteUserURL);

            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpDelete) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
            ArrayList<String> subTests = new ArrayList<>();
            try {

                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean the created user.
                cleanUpUser(id, "Delete User");
                if (requestPaths[i].getTestCaseName() != "User not found error response") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not delete the default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            }

            if (response.getStatusLine().getStatusCode() == 204) {
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            } else if (requestPaths[i].getTestCaseName() == "User not found error response" &&
                    response.getStatusLine().getStatusCode() == 404) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Server successfully given the expected error 404 message",
                                ComplianceUtils.getWire(method, responseString, headerString,
                                        responseStatus, subTests)));

            } else {

                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "", ComplianceUtils.getWire(method, responseString, headerString,
                                responseStatus, subTests)));
            }
        }
        return testResults;
    }

    @Override
    public ArrayList<TestResult> searchMethodTest() throws GeneralComplianceException, ComplianceException {

        // Store test results.
        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        // Store userIDS of 5 users.
        ArrayList<String> userIDs = createTestsUsers();

        // Post bodies of search methods.
        ArrayList<String> definedSearchMethods = new ArrayList<>();

        definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedSearchUsersPayload1);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedSearchUsersPayload2);
        //definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload3);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Post user with filter and pagination query parameters");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Post user and validate error message");

        requestPaths = new RequestPath[]{requestPath1, requestPath2};

        for (int i = 0; i < requestPaths.length; i++) {

            String searchUsersUrl = null;
            searchUsersUrl = url + "/.search";

            HttpPost method = new HttpPost(searchUsersUrl);
            // Create user test.
            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");

            HttpResponse response = null;
            String responseString = "";
            String headerString = "";
            String responseStatus = "";
            Integer totalResults;
            // JSONObject jsonObj = null;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                // Create the request.
                HttpEntity entity = new ByteArrayEntity
                        (definedSearchMethods.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);

                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                // Get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

                if (requestPaths[i].getTestCaseName() != "Post user and validate error message") {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not create default user at url " + url,
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }

            }

            if (response.getStatusLine().getStatusCode() == 200) {
                // Obtain the schema corresponding to user.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                ArrayList<User> userList = new ArrayList<>();
                JSONObject jsonObjResponse = null;
                User user = null;
                try {

                    JSONObject jsonObj = new JSONObject(responseString);
                    jsonObjResponse = jsonObj;
                    JSONArray usersArray = jsonObj.getJSONArray("Resources");
                    totalResults = (Integer) jsonObjResponse.get("totalResults");
                    JSONObject tmp;

                    for (int j = 0; j < usersArray.length(); j++) {
                        tmp = usersArray.getJSONObject(j);
                        userList.add((User) jsonDecoder.decodeResource(tmp.toString(), schema, new User()));
                        try {
                            ResponseValidateTests.runValidateTests(userList.get(j), schema,
                                    null, null, method,
                                    responseString, headerString, responseStatus, subTests);

                        } catch (BadRequestException | CharonException e) {

                            testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Response Validation Error",
                                    ComplianceUtils.getWire(method, responseString, headerString,
                                            responseStatus, subTests)));
                            continue;
                        }
                    }

                } catch (JSONException e) {

//                    throw new ComplianceException(500, "Error in decoding the returned list resource.");
                    continue;
                } catch (BadRequestException | CharonException | InternalErrorException e) {

                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    continue;
                }

                if (totalResults == 5) {
                    testResults.add(new TestResult
                            (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                    "", ComplianceUtils.getWire(method, responseString,
                                    headerString, responseStatus, subTests)));
                }
            } else if (requestPaths[i].getTestCaseName() == "Post user and validate error message"
                    && response.getStatusLine().getStatusCode() == 400) {

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error 400 message",
                                ComplianceUtils.getWire(method, responseString,
                                        headerString, responseStatus, subTests)));

            } else {
                testResults.add(
                        new TestResult
                                (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                        "", ComplianceUtils.getWire(method, responseString,
                                        headerString, responseStatus, subTests)));
            }
        }

        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "Search users");
        }

        return testResults;
    }

    @Override
    public ArrayList<TestResult> executeAllTests() throws GeneralComplianceException, ComplianceException {

        return null;
    }
}
