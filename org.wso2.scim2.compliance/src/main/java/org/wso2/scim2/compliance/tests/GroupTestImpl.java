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
import org.wso2.charon3.core.objects.Group;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation of Group test cases.
 */
public class GroupTestImpl implements ResourceType {

    private final ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private final String url;
    private ArrayList<String> groupIDs = new ArrayList<>();
    private ArrayList<String> userIDs = new ArrayList<>();

    public GroupTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.GROUPS_ENDPOINT
        ;

    }

    /**
     * Create test users.
     *
     * @return
     * @throws ComplianceException
     * @throws GeneralComplianceException
     */
    private ArrayList<String> createTestsUsers(String noOfUsers) throws ComplianceException,
            GeneralComplianceException {

        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;

        ArrayList<String> definedUsers = new ArrayList<>();

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
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus, subTests)));
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
    private boolean cleanUpUser(String id, String testName) throws GeneralComplianceException, ComplianceException {

        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;

        String deleteUserURL = url + "/" + id;

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
     * Create test groups.
     *
     * @return groupIds
     * @throws ComplianceException
     * @throws GeneralComplianceException
     */
    private ArrayList<String> createTestsGroups() throws ComplianceException, GeneralComplianceException {

        createTestsUsers("Many");

        ArrayList<String> definedGroups = new ArrayList<>();
        definedGroups.add("{\"displayName\": \"EYtXcD21\"}");
        definedGroups.add("{\"displayName\": \"BktqER22\"}");
        definedGroups.add("{\"displayName\": \"ZwLtOP23\"}");
        definedGroups.add("{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
                "\"displayName\":\"XwLtOP23\",\"members\":[{\"value\":\"" + userIDs.get(0) + "\",\"displayName" +
                "\":\"loginUser1\"}," +
                "{\"value\":\"" + userIDs.get(1) + "\",\"displayName\":\"loginUser2\"},{\"value\":\"" + userIDs.get(2) +
                "\",\"displayName\":\"loginUser3\"},{\"value\":\"" + userIDs.get(3) + "\",\"displayName" +
                "\":\"loginUser4" +
                "\"}," +
                "{\"value\":\"" + userIDs.get(4) + "\",\"displayName\":\"loginUser5\"}]}");

        HttpPost method = new HttpPost(url);
        //create groups
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder();
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        for (int i = 0; i < definedGroups.size(); i++) {
            try {
                //create the group
                HttpEntity entity = new ByteArrayEntity(definedGroups.get(i).getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    //obtain the schema corresponding to group
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

                    JSONDecoder jsonDecoder = new JSONDecoder();
                    Group group;
                    try {
                        group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());
                    } catch (BadRequestException | CharonException | InternalErrorException e) {
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Groups",
                                "Could not decode the server response of groups create.",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests)));
                    }
                    groupIDs.add(group.getId());
                }

            } catch (Exception e) {
                // Read the response body.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                       headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Groups",
                        "Could not create default groups at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests)));
            }
        }
        return groupIDs;
    }

    /**
     * This method cleans the group with the given groupId and the user with the given id.
     *
     * @param groupId  contains id
     * @param testName contains name of the test
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    public boolean cleanUpGroup(String groupId, String testName)
            throws GeneralComplianceException, ComplianceException {

        String deleteGroupURL = null;
        deleteGroupURL = url + "/" + groupId;

        HttpDelete method = new HttpDelete(deleteGroupURL);

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
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                            subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 204) {
            return true;
        } else {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                            subTests)));
        }
    }

    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        RequestPath[] requestPaths;

        createTestsGroups();

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("List groups");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?filter=displayName+eq+EYtXcD21");
        requestPath2.setTestCaseName("Get groups with Filter");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?startIndex=1&count=2");
        requestPath3.setTestCaseName("Get users with Pagination");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl("?sortBy=id&sortOrder=ascending");
        requestPath4.setTestCaseName("Sort test");

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setUrl("?filter=userName+eq+loginUser1&startIndex=1&count=1");
        requestPath5.setTestCaseName("Filter with pagination test");

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setUrl("?startIndex=-1&count=2");
        requestPath6.setTestCaseName("Get users having negative number as index");

        RequestPath requestPath7 = new RequestPath();
        requestPath7.setUrl("?count=2");
        requestPath7.setTestCaseName("Get users without index and only using count");

        RequestPath requestPath8 = new RequestPath();
        requestPath8.setUrl("?attributes=userName,name.givenName");
        requestPath8.setTestCaseName("Get users with specific attributes");

        RequestPath requestPath9 = new RequestPath();
        requestPath9.setUrl("?excludedAttributes=name.givenName,emails");
        requestPath9.setTestCaseName("Get users with excluding attributes");

        // This array hold the sub tests details.
        requestPaths = new RequestPath[]{requestPath1, requestPath2};
//        , requestPath3, requestPath4, requestPath5,
//                requestPath6, requestPath7, requestPath8, requestPath9};

        for (int i = 0; i < requestPaths.length; i++) {
            String requestUrl = url + requestPaths[i].getUrl();
            HttpGet method = new HttpGet(requestUrl);

            HttpClient client = HTTPClient.getHttpClient();

            method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");

            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                //get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                       headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                //get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                       headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                        "Could not list the groups at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests)));
            }

            if (response.getStatusLine().getStatusCode() == 200) {

                //obtain the schema corresponding to group
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                ArrayList<Group> groupList = new ArrayList<>();
                try {
                    JSONObject jsonObj = new JSONObject(responseString);
                    JSONArray groupsArray = jsonObj.getJSONArray("Resources");
                    JSONObject tmp;
                    for (int j = 0; j < groupsArray.length(); j++) {
                        tmp = groupsArray.getJSONObject(j);
                        groupList.add((Group) jsonDecoder.decodeResource(tmp.toString(), schema, new Group()));
                        try {
                            ResponseValidateTests.runValidateTests(groupList.get(j), schema,
                                    null, null, method,
                                    responseString, headerString.toString(), responseStatus, subTests);

                        } catch (BadRequestException | CharonException e) {

                            testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Response Validation Error",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests)));
                        }
                    }
                } catch (JSONException e) {

                    throw new ComplianceException(500, "Error in decoding the returned list resource.");

                } catch (BadRequestException | CharonException | InternalErrorException e) {

                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                }
                if (requestPaths[i].getTestCaseName() == "List groups") { // check for all created groups
                    try {

                        subTests.add(ComplianceConstants.TestConstants.ALL_GROUPS_IN_TEST);

                        ArrayList<String> returnedGroupIDs = new ArrayList<>();
                        for (Group group : groupList) {
                            returnedGroupIDs.add(group.getId());
                        }
                        for (String id : groupIDs) {
                            if (!returnedGroupIDs.contains(id)) {

                                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                        "Response does not contain all the created groups",
                                        ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                                responseStatus, subTests)));
                            }
                        }

                    } catch (CharonException e) {

                        throw new ComplianceException(500, "Could not get the created group id");
                    }
                } else if (requestPaths[i].getTestCaseName() == "Get groups with Filter") {
                    subTests.add(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST);

                    String value = "EYtXcD21";
                    for (Group group : groupList) {
                        try {
                            if (!Objects.equals(value, group.getDisplayName())) {

                                testResults.add(new TestResult(TestResult.ERROR, "Filter Groups",
                                        "Response does not contain the expected groups",
                                        ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                                responseStatus, subTests)));
                                continue;
                            }
                        } catch (CharonException e) {
                            continue;
                        }
                    }
                }

                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
            } else {
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
            }
        }

        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "get users test");
        }

        for (String id : groupIDs) {
            cleanUpGroup(id, "Get groups");
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
