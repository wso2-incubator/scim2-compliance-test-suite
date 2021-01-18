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
import org.wso2.scim2.compliance.tests.model.RequestPath;
import org.wso2.scim2.compliance.utils.ComplianceConstants;

import java.util.ArrayList;

/**
 * Implementation of Bulk test cases.
 */
public class BulkTestImpl implements ResourceType {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;

    /**
     * Initializer.
     *
     * @param complianceTestMetaDataHolder
     */
    public BulkTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.BULK_ENDPOINT;
    }

    /**
     * Extract created user locations from the bulk response.
     *
     * @param response
     * @return
     * @throws JSONException
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
     * @param location
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    private boolean cleanUp(String location, String testName) throws GeneralComplianceException, ComplianceException {

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

    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        return null;
    }

    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        ArrayList<String> createdResourceLocations = new ArrayList<>();
        ArrayList<String> userIDs = new ArrayList<>();
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
                "                    },\n" +
                "                     {\n" +
                "                    \"type\": \"User\",\n" +
                "                     \"value\": \"" + userIDs.get(2) + "\"\n" +
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
                "                      \"value\": \"" + userIDs.get(3) + "\"\n" +
                "                    },\n" +
                "                     {\n" +
                "                    \"type\": \"User\",\n" +
                "                     \"value\": \"" + userIDs.get(4) + "\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "     ]\n" +
                "}");

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
        requestPath4.setTestCaseName("Bulk post operation with groups and users");
        try {
            requestPath4.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath4.setTestSupported(true);
        }

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setTestCaseName("Patch User error validation");
        try {
            requestPath5.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath5.setTestSupported(true);
        }

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setTestCaseName("Patch Enterprise User with array of operations");
        try {
            requestPath6.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig()
                    .getBulkSupported());
        } catch (Exception e) {
            requestPath6.setTestSupported(true);
        }

        requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3};

        for (int i = 0; i < requestPaths.length; i++) {
            HttpPost method = new HttpPost(url);
            // Create test.
            HttpClient client = HTTPClient.getHttpClient();
            method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus = StringUtils.EMPTY;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                HttpEntity entity = new ByteArrayEntity(definedBulkRequests.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
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
                //  Get the created user locations.
                createdResourceLocations = getLocations(responseString);
            } catch (Exception e) {
                // Read the response body.
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPaths[i].getTestSupported() != false) {
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not perform bulk request at " + url,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests)));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                //run clean up task
                for (String location : createdResourceLocations) {
                    cleanUp(location, requestPaths[i].getTestCaseName());
                }
                testResults.add(new TestResult
                        (TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
            } else if (requestPaths[i].getTestSupported() == false) {
                testResults.add(new TestResult
                        (TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                                "This functionality is not implemented. Hence given status code 501",
                                ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(),
                                        responseStatus, subTests)));
            } else {
                testResults.add(new TestResult
                        (TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests)));
            }
        }
        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUp(id, "User");
        }
        return testResults;
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
