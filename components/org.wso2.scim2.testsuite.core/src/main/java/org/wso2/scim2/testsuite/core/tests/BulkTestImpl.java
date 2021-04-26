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
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
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
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;
import org.wso2.scim2.testsuite.core.httpclient.HTTPClient;
import org.wso2.scim2.testsuite.core.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;
import org.wso2.scim2.testsuite.core.tests.model.RequestPath;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Implementation of Bulk test cases.
 */
public class BulkTestImpl implements ResourceType {

    private final ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private final String url;

    /**
     * Initializer.
     *
     * @param complianceTestMetaDataHolder Stores data required to run tests.
     */
    public BulkTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.BULK_ENDPOINT;
    }

    /**
     * Extract created user/group locations from the bulk response.
     *
     * @param response Json response from service provider.
     * @return userLocations Array of resource locations.
     * @throws JSONException Json exception if response not contain location attribute.
     */
    public ArrayList<String> getLocations(String response) throws JSONException {

        ArrayList<String> userLocations = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonarray = jsonObject.optJSONArray("Operations");
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject innerJsonObject = jsonarray.getJSONObject(i);
            String location = innerJsonObject.getString("location");
            userLocations.add(location);
        }
        return userLocations;
    }

    /**
     * Extract created status from the bulk response.
     *
     * @param response Json response from service provider.
     * @return statusCodes Array of status codes of resources.
     * @throws JSONException Json exception if response not contain expected attributes.
     */
    public ArrayList<Integer> getStatus(String response) throws JSONException {

        ArrayList<Integer> statusCodes = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonarray = jsonObject.optJSONArray("Operations");
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject innerJsonObject = jsonarray.getJSONObject(i);
            JSONObject innerJsonObject2 = innerJsonObject.getJSONObject("status");
            int code = innerJsonObject2.getInt("code");
            statusCodes.add(code);
        }
        return statusCodes;
    }

    /**
     * Create test users for test cases.
     *
     * @param noOfUsers Specify the number of users needs to create.
     * @return userIDS of created users.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     * @throws GeneralComplianceException General exceptions.
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
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        for (String definedUser : definedUsers) {
            long startTime = System.currentTimeMillis();
            try {
                // Create Users.
                HttpEntity entity = new ByteArrayEntity(definedUser.getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    // Obtain the schema corresponding to the user.
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                    JSONDecoder jsonDecoder = new JSONDecoder();
                    User user;
                    try {
                        user = jsonDecoder.decodeResource(responseString, schema, new User());
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
                assert response != null;
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
     * Create test groups for test cases.
     *
     * @param userIDs    Array of user ids to use as members in groups.
     * @param noOfGroups Specify the number of groups needs to create.
     * @return groupIDs of created groups.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     * @throws GeneralComplianceException General exceptions.
     */
    private ArrayList<String> createTestsGroups(ArrayList<String> userIDs, String noOfGroups) throws
            ComplianceException, GeneralComplianceException {

        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.GROUPS_ENDPOINT;

        ArrayList<String> groupIDs = new ArrayList<>();
        ArrayList<String> definedGroups = new ArrayList<>();

        if (noOfGroups.equals("One")) {
            definedGroups.add("{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
                    "\"displayName\":\"XwLtOP23\",\"members\":[{\"value\":\"" + userIDs.get(0) + "\",\"displayName" +
                    "\":\"loginUser1\",\"$ref\":\"" + complianceTestMetaDataHolder.getUrl() +
                    ComplianceConstants.TestConstants.USERS_ENDPOINT + "/" + userIDs.get(0) + "\"}," +
                    "{\"value\":\"" + userIDs.get(1) + "\",\"displayName\":\"loginUser2\"},{\"value\":\"" +
                    userIDs.get(2) + "\",\"displayName\":\"loginUser3\"},{\"value\":\"" + userIDs.get(3) +
                    "\",\"displayName" + "\":\"loginUser4" + "\"}," +
                    "{\"value\":\"" + userIDs.get(4) + "\",\"displayName\":\"loginUser5\"}]}");
        } else if (noOfGroups.equals("Many")) {
            definedGroups.add("{\"displayName\": \"EYtXcD21\"}");
            definedGroups.add("{\"displayName\": \"BktqER22\"}");
            definedGroups.add("{\"displayName\": \"ZwLtOP23\"}");
            definedGroups.add("{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
                    "\"displayName\":\"XwLtOP23\",\"members\":[{\"value\":\"" + userIDs.get(0) + "\",\"displayName" +
                    "\":\"loginUser1\"}," + "{\"value\":\"" + userIDs.get(1) + "\",\"displayName\":\"loginUser2\"}," +
                    "{\"value\":\"" + userIDs.get(2) + "\",\"displayName\":\"loginUser3\"},{\"value\":\"" +
                    userIDs.get(3) + "\",\"displayName" + "\":\"loginUser4" + "\"}," +
                    "{\"value\":\"" + userIDs.get(4) + "\",\"displayName\":\"loginUser5\"}]}");
        }

        HttpPost method = new HttpPost(url);
        // Create groups.
        HttpClient client = HTTPClient.getHttpClient();
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder();
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        for (String definedGroup : definedGroups) {
            long startTime = System.currentTimeMillis();
            try {
                // Create the group.
                HttpEntity entity = new ByteArrayEntity(definedGroup.getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    // Obtain the schema corresponding to group.
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                    JSONDecoder jsonDecoder = new JSONDecoder();
                    Group group;
                    try {
                        group = jsonDecoder.decodeResource(responseString, schema, new Group());
                    } catch (BadRequestException | CharonException | InternalErrorException e) {
                        long stopTime = System.currentTimeMillis();
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Groups",
                                "Could not decode the server response of groups create.",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests), stopTime - startTime));
                    }
                    groupIDs.add(group.getId());
                }
            } catch (Exception e) {
                // Read the response body.
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                long stopTime = System.currentTimeMillis();
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Groups",
                        "Could not create default groups at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests), stopTime - startTime));
            }
        }
        return groupIDs;
    }

    /**
     * This method cleans up resources.
     *
     * @param location Resource location.
     * @param testName Related test case name.
     * @return true or false.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    private boolean cleanUp(String location, String testName) throws GeneralComplianceException, ComplianceException {

        long startTime = System.currentTimeMillis();
        String deleteUserURL;

        if (testName.equals("User")) {
            String url = complianceTestMetaDataHolder.getUrl() +
                    ComplianceConstants.TestConstants.USERS_ENDPOINT;
            deleteUserURL = url + "/" + location;
        } else if (testName.equals("Group")) {
            String url = complianceTestMetaDataHolder.getUrl() +
                    ComplianceConstants.TestConstants.GROUPS_ENDPOINT;
            deleteUserURL = url + "/" + location;
        } else {
            deleteUserURL = location;
        }

        HttpDelete method = new HttpDelete(deleteUserURL);
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
            /*
             Read the response body.
             Get all headers.
             */
            assert response != null;
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
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
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
     * Get bulk tests. This method is not valid for bulk according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-3.7
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    /**
     * Get bulk by id tests. This method is not valid for bulk according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-3.7
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
     * Post bulk tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        ArrayList<Integer> resourceStatusCodes = new ArrayList<>();
        ArrayList<String> createdResourceLocations = new ArrayList<>();
        ArrayList<String> userIDs;
        userIDs = createTestsUsers("Many");

        ArrayList<String> definedBulkRequests = new ArrayList<>();

        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest1);
        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest2);
        definedBulkRequests.add(" {\n" +
                "    \"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\": [\n" +
                "       {\n" +
                "            \"method\": \"POST\",\n" +
                "            \"path\": \"/Groups\",\n" +
                "            \"bulkId\": \"ytrewq\",\n" +
                "            \"data\": {\n" +
                "                \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"],\n" +
                "                \"displayName\": \"xc12323\",\n" +
                "                \"members\": [\n" +
                "                    {\n" +
                "                    \"type\": \"User\",\n" +
                "                    \"value\": \"" + userIDs.get(0) + "\"\n" +
                "                    },\n" +
                "                     {\n" +
                "                    \"type\": \"User\",\n" +
                "                     \"value\": \"" + userIDs.get(1) + "\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "               {\n" +
                "            \"method\": \"POST\",\n" +
                "            \"path\": \"/Groups\",\n" +
                "            \"bulkId\": \"ytrewq\",\n" +
                "            \"data\": {\n" +
                "                \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"],\n" +
                "                \"displayName\": \"vd3343\",\n" +
                "                \"members\": [\n" +
                "                    {\n" +
                "                    \"type\": \"User\",\n" +
                "                      \"value\": \"" + userIDs.get(2) + "\"\n" +
                "                    },\n" +
                "                     {\n" +
                "                    \"type\": \"User\",\n" +
                "                     \"value\": \"" + userIDs.get(3) + "\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "     ]\n" +
                "}");

        definedBulkRequests.add(" {\"failOnErrors\":0,\n" +
                "    \"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\": [\n" +
                "       {\n" +
                "            \"method\": \"POST\",\n" +
                "            \"path\": \"/Users\",\n" +
                "            \"bulkId\": \"qwerty\",\n" +
                "            \"data\": {\n" +
                "                \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:User\"],\n" +
                "                \"userName\": \"loginUser-11\",\n" +
                "                \"password\":\"kim123\",\n" +
                "                \"name\": {\n" +
                "                    \"givenName\": \"Kim\",\n" +
                "                    \"familyName\": \"Berry\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "               {\n" +
                "            \"method\": \"POST\",\n" +
                "            \"path\": \"/Groups\",\n" +
                "            \"bulkId\": \"ytrewq\",\n" +
                "            \"data\": {\n" +
                "                \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"],\n" +
                "                \"displayName\": \"xa24121\",\n" +
                "                \"members\": [\n" +
                "                    {\n" +
                "                    \"value\": \"" + userIDs.get(4) + "\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "     ]\n" +
                "}");

        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest3);
        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest4);
        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest5);
        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest6);
        definedBulkRequests.add(ComplianceConstants.DefinedInstances.defineBulkRequest7);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Bulk post operation with users");
        try {
            requestPath1.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath1.setTestSupported(true);
        }

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Bulk post operation with groups");
        try {
            requestPath2.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath2.setTestSupported(true);
        }

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Bulk post operation with groups with members");
        try {
            requestPath3.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath3.setTestSupported(true);
        }

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Bulk post operation with users and groups");
        try {
            requestPath4.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath4.setTestSupported(true);
        }

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setTestCaseName("Bulk post operation without bulk Id");
        try {
            requestPath5.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath5.setTestSupported(true);
        }

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setTestCaseName("Bulk post operation without path");
        try {
            requestPath6.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath6.setTestSupported(true);
        }

        RequestPath requestPath7 = new RequestPath();
        requestPath7.setTestCaseName("Bulk post operation without data");
        try {
            requestPath7.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath7.setTestSupported(true);
        }

        RequestPath requestPath8 = new RequestPath();
        requestPath8.setTestCaseName("Bulk post operation with temporary id");
        try {
            requestPath8.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath8.setTestSupported(true);
        }

        RequestPath requestPath9 = new RequestPath();
        requestPath9.setTestCaseName("Bulk post operation with fail on errors");
        try {
            requestPath9.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath9.setTestSupported(true);
        }

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3, requestPath4, requestPath5,
                requestPath6, requestPath7, requestPath8, requestPath9};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            HttpPost method = new HttpPost(url);
            // Create test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                HttpEntity entity = new ByteArrayEntity(definedBulkRequests.get(i).getBytes(StandardCharsets.UTF_8));
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
                //  Get the created user locations.
                resourceStatusCodes = getStatus(responseString);
                //  Get the created user locations.
                createdResourceLocations = getLocations(responseString);
            } catch (Exception e) {
                /*
                 Read the response body.
                 Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPaths[i].getTestSupported() &&
                        !requestPaths[i].getTestCaseName().equals("Bulk post operation without bulk Id") &&
                        !requestPaths[i].getTestCaseName().equals("Bulk post operation without path") &&
                        !requestPaths[i].getTestCaseName().equals("Bulk post operation without data") &&
                        !requestPaths[i].getTestCaseName().equals("Bulk post operation with fail on errors")) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not perform bulk request at " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            boolean pass = false;
            for (Integer status : resourceStatusCodes) {
                if (status == HttpStatus.SC_CREATED) {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_CREATED);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                    subTests.add(StringUtils.EMPTY);
                    pass = true;
                } else {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_CREATED);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    pass = false;
                    break;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && pass) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                // Run clean up task.
                for (String location : createdResourceLocations) {
                    cleanUp(location, requestPaths[i].getTestCaseName());
                }
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Bulk post operation without bulk Id") &&
                    response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_BAD_REQUEST);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error \"JSON string could not be " +
                                        "decoded properly .Required attribute BULK_ID is missing in the request\"",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Bulk post operation without path") &&
                    response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_BAD_REQUEST);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error \"JSON string could not be " +
                                        "decoded properly .Required attribute path is missing in the request\"",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Bulk post operation without data") &&
                    response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_BAD_REQUEST);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                "Service Provider successfully given the expected error \"JSON string could not be " +
                                        "decoded properly .Required attribute data is missing in the request\"",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Bulk post operation with fail on errors") &&
                    response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                int n = 0;
                boolean failOnErrorPass = true;
                for (Integer status : resourceStatusCodes) {
                    if (n == 0 && status == 201) {
                        subTests.add("First resource - loginUser21");
                        subTests.add("Message : Created the resource hence given 201");
                        subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                        subTests.add(StringUtils.EMPTY);
                    } else if (n == 1 && status == 409) {
                        subTests.add("Second resource - loginUser1");
                        subTests.add("Message : User already exists hence given 409");
                        subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                        subTests.add(StringUtils.EMPTY);
                    } else {
                        subTests.add("Third resource");
                        subTests.add("Message : Fail on errors is not working");
                        subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                        subTests.add(StringUtils.EMPTY);
                        failOnErrorPass = false;
                    }
                    n++;
                }
                String location;
                JSONObject innerJsonObject;
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray jsonarray = jsonObject.optJSONArray("Operations");
                    innerJsonObject = jsonarray.getJSONObject(0);
                    location = innerJsonObject.getString("location");
                } catch (Exception e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the location from server response",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
                // Run clean up task.
                cleanUp(location, requestPaths[i].getTestCaseName());
                // Check for status returned.
                subTests.add("Fail on errors test");
                subTests.add("Message : Out of 3 operations only 2 executed since fail on errors is 1 and 2nd " +
                        "operation failed.");
                if (failOnErrorPass) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult
                            (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                    "",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests), stopTime - startTime));
                } else {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult
                            (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Service Provider failed to give the expected result",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests), stopTime - startTime));
                }
            } else if (!requestPaths[i].getTestSupported()) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            }
        }
        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUp(id, "User");
        }
        return testResults;
    }

    /**
     * Patch bulk tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        ArrayList<Integer> resourceStatusCodes = new ArrayList<>();
        ArrayList<String> userIDs;
        ArrayList<String> groupIDs;
        userIDs = createTestsUsers("Many");
        groupIDs = createTestsGroups(userIDs, "Many");

        ArrayList<String> definedBulkRequests = new ArrayList<>();

        definedBulkRequests.add("{ \n" +
                "    \"failOnErrors\": 1, \n" +
                "    \"schemas\": [ \"urn:ietf:params:scim:api:messages:2.0:BulkRequest\" ], \n" +
                "    \"Operations\": [\n" +
                "        { \n" +
                "            \"method\": \"PATCH\", \n" +
                "            \"path\": \"/Users/" + userIDs.get(0) + "\", \n" +
                "            \"data\": { \n" +
                "                \"Operations\": [\n" +
                "                    { \n" +
                "                        \"op\": \"replace\", \n" +
                "                        \"path\": \"name\", \n" +
                "                        \"value\": \n" +
                "                        { \n" +
                "                            \"givenName\": \"john\", \n" +
                "                            \"familyName\": \"Anderson\" \n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"op\": \"add\", \n" +
                "                        \"path\": \"nickName\", \n" +
                "                        \"value\": \"shaggy\"  \n" +
                "                    }\n" +
                "                ] \n" +
                "            } \n" +
                "        },\n" +
                "        { \n" +
                "            \"method\": \"PATCH\", \n" +
                "            \"path\": \"/Users/" + userIDs.get(1) + "\", \n" +
                "            \"data\": { \n" +
                "                \"Operations\": [{ \n" +
                "                    \"op\": \"remove\", \n" +
                "                    \"path\": \"emails[type eq home]\"\n" +
                "                }] \n" +
                "            } \n" +
                "        }\n" +
                "    ] \n" +
                "}");

        definedBulkRequests.add("{ \n" +
                "    \"schemas\": [ \"urn:ietf:params:scim:api:messages:2.0:BulkRequest\" ], \n" +
                "    \"Operations\": [\n" +
                " {\n" +
                "           \"method\": \"PATCH\",\n" +
                "           \"path\": \"/Groups/" + groupIDs.get(0) + "\",\n" +
                "           \"data\": {\n" +
                "               \"Operations\": [\n" +
                "                   {\n" +
                "                       \"op\": \"add\",\n" +
                "                       \"value\": {\"members\":[\n" +
                "                           {\n" +
                "                           \"display\": \"loginUser1\",\n" +
                "                           \"value\": \"" + userIDs.get(0) + "\"\n" +
                "                           }\n" +
                "                       ]}\n" +
                "                   }\n" +
                "               ]\n" +
                "           }\n" +
                "       },\n" +
                "        {\n" +
                "           \"method\": \"PATCH\",\n" +
                "           \"path\": \"/Groups/" + groupIDs.get(3) + "\",\n" +
                "           \"data\": {\n" +
                "               \"Operations\": [\n" +
                "                   {\n" +
                "                        \"op\":\"remove\",\n" +
                "                        \"path\":\"members[value eq \\\"" + userIDs.get(0) + "\\\"]\"\n" +
                "                    }\n" +
                "               ]\n" +
                "           }\n" +
                "       }\n" +
                "    ] \n" +
                "}");

        definedBulkRequests.add("{ \n" +
                "    \"schemas\": [ \"urn:ietf:params:scim:api:messages:2.0:BulkRequest\" ], \n" +
                "    \"Operations\": [\n" +
                " {\n" +
                "           \"method\": \"PATCH\",\n" +
                "           \"path\": \"/Groups/" + groupIDs.get(1) + "\",\n" +
                "           \"data\": {\n" +
                "               \"Operations\": [\n" +
                "                   {\n" +
                "                       \"op\": \"add\",\n" +
                "                       \"value\": {\"members\":[\n" +
                "                           {\n" +
                "                           \"display\": \"loginUser1\",\n" +
                "                           \"value\": \"" + userIDs.get(0) + "\"\n" +
                "                           }\n" +
                "                       ]}\n" +
                "                   }\n" +
                "               ]\n" +
                "           }\n" +
                "       },\n" +
                "        { \n" +
                "            \"method\": \"PATCH\", \n" +
                "            \"path\": \"/Users/" + userIDs.get(1) + "\", \n" +
                "            \"data\": { \n" +
                "                \"Operations\": [{ \n" +
                "                    \"op\": \"remove\", \n" +
                "                    \"path\": \"emails[type eq home]\"\n" +
                "                }] \n" +
                "            } \n" +
                "        }\n" +
                "    ] \n" +
                "}");

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Bulk patch operation with users");
        try {
            requestPath1.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath1.setTestSupported(true);
        }

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Bulk patch operation with groups");
        try {
            requestPath2.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath2.setTestSupported(true);
        }

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Bulk patch operation with users and groups");
        try {
            requestPath3.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath3.setTestSupported(true);
        }

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            HttpPost method = new HttpPost(url);
            // Create test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                HttpEntity entity = new ByteArrayEntity(definedBulkRequests.get(i).getBytes(StandardCharsets.UTF_8));
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
                //  Get the created user locations.
                resourceStatusCodes = getStatus(responseString);
            } catch (Exception e) {
                /*
                 Read the response body.
                 Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPaths[i].getTestSupported()) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not perform bulk request at " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            boolean pass = false;
            for (Integer status : resourceStatusCodes) {
                if (status == HttpStatus.SC_OK) {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                    subTests.add(StringUtils.EMPTY);
                    pass = true;
                } else {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    pass = false;
                    break;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && pass) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            } else if (!requestPaths[i].getTestSupported()) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            }
        }
        // Clean up users.
        for (String id : userIDs) {
            cleanUp(id, "User");
        }
        // Clean up groups.
        for (String id : groupIDs) {
            cleanUp(id, "Group");
        }
        return testResults;
    }

    /**
     * Put bulk tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        ArrayList<Integer> resourceStatusCodes = new ArrayList<>();
        ArrayList<String> userIDs;
        ArrayList<String> groupIDs;
        userIDs = createTestsUsers("Many");
        groupIDs = createTestsGroups(userIDs, "Many");

        ArrayList<String> definedBulkRequests = new ArrayList<>();

        definedBulkRequests.add("{\n" +
                "    \"failOnErrors\":1,\n" +
                "    \"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\":[\n" +
                "        {\n" +
                "            \"method\": \"PUT\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(0) + "\",\n" +
                "            \"bulkId\": \"qwerty\",\n" +
                "            \"data\":{\n" +
                "                \"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],\n" +
                "                \"userName\": \"loginUser1\",\n" +
                "                \"name\": {\n" +
                "                    \"givenName\": \"John\",\n" +
                "                    \"familyName\": \"Berry\"\n" +
                "                },\n" +
                "                \"emails\": [\n" +
                "                    {\n" +
                "                        \"type\": \"home\",\n" +
                "                        \"value\": \"john@gmail.com\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"PUT\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(1) + "\",\n" +
                "            \"bulkId\":\"ytrewq\",\n" +
                "            \"data\":{\n" +
                "                \"schemas\":[\n" +
                "                \"urn:ietf:params:scim:schemas:core:2.0:User\",\n" +
                "                \"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"\n" +
                "                ],\n" +
                "                \"userName\":\"loginUser2\",\n" +
                "                \"name\": {\n" +
                "                    \"givenName\": \"Smith\",\n" +
                "                    \"familyName\": \"Berry\"\n" +
                "                },\n" +
                "                \"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\":{\n" +
                "                    \"employeeNumber\": \"12345\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        definedBulkRequests.add("{\n" +
                "    \"failOnErrors\":1,\n" +
                "    \"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\":[\n" +
                "        {\n" +
                "            \"method\": \"PUT\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(0) + "\",\n" +
                "            \"data\":{\n" +
                "                \"displayName\": \"xs5231771\",\n" +
                "                \"members\": [\n" +
                "                    {\n" +
                "                    \"display\": \"loginUser1\",\n" +
                "                    \"value\": \"" + userIDs.get(0) + "\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"PUT\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(1) + "\",\n" +
                "            \"data\":{\n" +
                "                \"displayName\": \"Fvs132312\",\n" +
                "                \"members\": [\n" +
                "                    {\n" +
                "                    \"display\": \"loginUser2\",\n" +
                "                    \"value\": \"" + userIDs.get(1) + "\"\n" +
                "                    }\n" +
                "            ] \n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        definedBulkRequests.add("{\n" +
                "    \"failOnErrors\":1,\n" +
                "    \"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\":[\n" +
                "        {\n" +
                "            \"method\": \"PUT\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(2) + "\",\n" +
                "            \"bulkId\": \"qwerty\",\n" +
                "            \"data\":{\n" +
                "                \"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],\n" +
                "                \"userName\": \"loginUser3\",\n" +
                "                \"name\": {\n" +
                "                    \"givenName\": \"John\",\n" +
                "                    \"familyName\": \"Berry\"\n" +
                "                },\n" +
                "                \"emails\": [\n" +
                "                    {\n" +
                "                        \"type\": \"home\",\n" +
                "                        \"value\": \"john@gmail.com\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"PUT\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(2) + "\",\n" +
                "            \"data\":{\n" +
                "                \"displayName\": \"gfS232324\",\n" +
                "                \"members\": [\n" +
                "                    {\n" +
                "                    \"display\": \"loginUser4\",\n" +
                "                    \"value\": \"" + userIDs.get(3) + "\"\n" +
                "                    }\n" +
                "            ] \n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Bulk put operation with users");
        try {
            requestPath1.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath1.setTestSupported(true);
        }

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Bulk put operation with groups");
        try {
            requestPath2.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath2.setTestSupported(true);
        }

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Bulk put operation with users and groups");
        try {
            requestPath3.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath3.setTestSupported(true);
        }

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            HttpPost method = new HttpPost(url);
            // Create test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                HttpEntity entity = new ByteArrayEntity(definedBulkRequests.get(i).getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                //  Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();
                //  Get the created user locations.
                resourceStatusCodes = getStatus(responseString);
            } catch (Exception e) {
                /*
                 Read the response body.
                 Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPaths[i].getTestSupported()) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not perform bulk request at " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            boolean pass = false;
            for (Integer status : resourceStatusCodes) {
                if (status == HttpStatus.SC_OK) {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                    subTests.add(StringUtils.EMPTY);
                    pass = true;
                } else {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    pass = false;
                    break;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && pass) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            } else if (!requestPaths[i].getTestSupported()) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            }
        }
        // Clean up users.
        for (String id : userIDs) {
            cleanUp(id, "User");
        }
        // Clean up groups.
        for (String id : groupIDs) {
            cleanUp(id, "Group");
        }
        return testResults;
    }

    /**
     * Delete bulk tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException        Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        ArrayList<Integer> resourceStatusCodes = new ArrayList<>();
        ArrayList<String> userIDs;
        ArrayList<String> groupIDs;
        boolean error = false;
        userIDs = createTestsUsers("Many");
        groupIDs = createTestsGroups(userIDs, "Many");

        ArrayList<String> definedBulkRequests = new ArrayList<>();

        definedBulkRequests.add("{\n" +
                "    \"failOnErrors\":1,\n" +
                "    \"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\":[\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(0) + "\"  \n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(1) + "\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(2) + "\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");
        definedBulkRequests.add("{\n" +
                "    \"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\":[\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(0) + "\"  \n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(1) + "\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        definedBulkRequests.add("{\n" +
                "    \"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\":[\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(2) + "\"  \n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Groups/" + groupIDs.get(3) + "\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(3) + "\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"method\": \"DELETE\",\n" +
                "            \"path\": \"/Users/" + userIDs.get(4) + "\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Bulk delete operation with users");
        try {
            requestPath1.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath1.setTestSupported(true);
        }

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Bulk delete operation with groups");
        try {
            requestPath2.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath2.setTestSupported(true);
        }

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Bulk delete operation with users and groups");
        try {
            requestPath3.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath3.setTestSupported(true);
        }

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3};

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            HttpPost method = new HttpPost(url);
            // Create test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                HttpEntity entity = new ByteArrayEntity(definedBulkRequests.get(i).getBytes(StandardCharsets.UTF_8));
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
                //  Get the created user locations.
                resourceStatusCodes = getStatus(responseString);
            } catch (Exception e) {
                /*
                 Read the response body.
                 Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                error = true;
                if (requestPaths[i].getTestSupported()) {
                    // Check for status returned.
                    subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not perform bulk request at " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests), stopTime - startTime));
                    continue;
                }
            }
            boolean pass = false;
            for (Integer status : resourceStatusCodes) {
                if (status == HttpStatus.SC_NO_CONTENT) {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_NO_CONTENT);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                    subTests.add(StringUtils.EMPTY);
                    pass = true;
                } else {
                    // Check for status returned.
                    subTests.add("Check each resource status");
                    subTests.add(ComplianceConstants.TestConstants.ACTUAL + status);
                    subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_NO_CONTENT);
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    pass = false;
                    break;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && pass) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            } else if (!requestPaths[i].getTestSupported()) {
                // Check for status returned.
                subTests.add(ComplianceConstants.TestConstants.STATUS_CODE);
                subTests.add(ComplianceConstants.TestConstants.ACTUAL + response.getStatusLine().getStatusCode());
                subTests.add(ComplianceConstants.TestConstants.EXPECTED + HttpStatus.SC_OK);
                subTests.add("Status : Skipped");
                subTests.add(StringUtils.EMPTY);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests), stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests), stopTime - startTime));
            }
        }

        if (error) {
            // Clean up users.
            for (String id : userIDs) {
                cleanUp(id, "User");
            }
            // Clean up groups.
            for (String id : groupIDs) {
                cleanUp(id, "Group");
            }
        }
        return testResults;
    }

    /**
     * Search bulk tests. This method is not valid for bulk according to the
     * RFC-7644 https://tools.ietf.org/html/rfc7644#section-3.7
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
