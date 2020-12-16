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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
        //create users
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
                //create the group
                HttpEntity entity = new ByteArrayEntity(definedUsers.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    //obtain the schema corresponding to group
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
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            // Read the response body.
            //get all headers
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

    private void initiateData() throws ComplianceException, GeneralComplianceException {
        // Initialize 5 users.
        createTestsUsers();

        // Creating objects to store sub test information.
        RequestPath obj1 = new RequestPath();
        obj1.setUrl("/");
        obj1.setTestCaseName("List Users");

        RequestPath obj2 = new RequestPath();
        obj2.setUrl("/" + userIDs.get(0));
        obj2.setTestCaseName("Get user by ID");

        RequestPath obj3 = new RequestPath();
        obj3.setUrl("/?filter=userName+eq+loginUser1");
        obj3.setTestCaseName("Get user with Filter");

        RequestPath obj4 = new RequestPath();
        obj4.setUrl("/?startIndex=1&count=2");
        obj4.setTestCaseName("Get users with Pagination");

        RequestPath obj5 = new RequestPath();
        obj5.setUrl("/?sortBy=id&sortOrder=ascending");
        obj5.setTestCaseName("Sort test");

        RequestPath obj6 = new RequestPath();
        obj6.setUrl("/?filter=userName+eq+loginUser1&startIndex=1&count=1");
        obj6.setTestCaseName("filter with pagination test");

        // This array hold the sub tests details.
        requestPaths = new RequestPath[]{obj1, obj2, obj3, obj4, obj5, obj6};

    }

    @Override
    public TestResult getMethodTest() throws GeneralComplianceException, ComplianceException {

        // Initiate data necessary for getMethod test.
        initiateData();

        for (int i = 0; i < requestPaths.length; i++) {
            System.out.println("Element at " + i + " : " + requestPaths[i].getTestCaseName());

            String requestUrl = url + requestPaths[i].getUrl();
            HttpGet method = new HttpGet(requestUrl);

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

                //obtain the schema corresponding to user
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                ArrayList<User> userList = new ArrayList<>();
                User user = null;
                try {
                    // Called only for user get by id.
                    if (requestPaths[i].getTestCaseName() == "Get user by ID") {

                        user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

                        try {
                            ResponseValidateTests.runValidateTests(user, schema, null,
                                    null, method,
                                    responseString, headerString, responseStatus, subTests);

                        } catch (BadRequestException | CharonException e) {

                            testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Response Validation Error",
                                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                            subTests)));
                            continue;
                        }

                    } else {
                        JSONObject jsonObj = new JSONObject(responseString);
                        JSONArray usersArray = jsonObj.getJSONArray("Resources");
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
                } else if (requestPaths[i].getTestCaseName() == "Get users with Pagination") {
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
                } else if (requestPaths[i].getTestCaseName() == "filter with pagination test") {

                    subTests.add(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION);

                    if (userList.size() != 1) {

                        testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                "Response does not contain right number of pagination.",
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
        return new TestResult();
    }

    @Override
    public TestResult postMethodTest() throws GeneralComplianceException, ComplianceException {

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
            // Get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            testResults.add(new TestResult(TestResult.ERROR, "Create User",
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
                try {
                    cleanUpUser(user.getId(), "Create User");
                } catch (CharonException e1) {
                    testResults.add(new TestResult(TestResult.ERROR, "Create User",
                            "Could not retrieve the user id",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                testResults.add(new TestResult(TestResult.ERROR, "Create User",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                try {
                    cleanUpUser(user.getId(), "Create User");
                } catch (CharonException e1) {
                    testResults.add(new TestResult(TestResult.ERROR, "Create User",
                            "Could not retrieve the user id",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                testResults.add(new TestResult(TestResult.ERROR, "Create User",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                cleanUpUser(user.getId(), "Create User");
            } catch (CharonException e1) {
                testResults.add(new TestResult(TestResult.ERROR, "Create User",
                        "Could not retrieve the user id",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            testResults.add(new TestResult
                    (TestResult.SUCCESS, "Create User",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests)));
        } else {
            testResults.add(
                    new TestResult
                            (TestResult.ERROR, "Create User",
                                    "", ComplianceUtils.getWire(method, responseString,
                                    headerString, responseStatus, subTests)));
        }
        return new TestResult();
    }

    @Override
    public TestResult patchMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public TestResult putMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public TestResult deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public TestResult searchMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public TestResult executeAllTests() throws GeneralComplianceException, ComplianceException {

        return null;
    }
}
