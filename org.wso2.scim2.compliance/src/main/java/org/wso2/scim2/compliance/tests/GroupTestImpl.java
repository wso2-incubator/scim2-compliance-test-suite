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
 * Implementation of Group test cases.
 */
public class GroupTestImpl implements ResourceType {

    private final ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private final String url;
    private ArrayList<String> groupIDs = new ArrayList<>();

    public GroupTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.GROUPS_ENDPOINT
        ;

    }

    /**
     * Create test groups.
     *
     * @return groupIds
     * @throws ComplianceException
     * @throws GeneralComplianceException
     */
    private ArrayList<String> createTestsGroups() throws ComplianceException, GeneralComplianceException {

        ArrayList<String> definedGroups = new ArrayList<>();
        definedGroups.add("{\"displayName\": \"EYtXcD21\"}");
        definedGroups.add("{\"displayName\": \"BktqER22\"}");
        definedGroups.add("{\"displayName\": \"ZwLtOP23\"}");

        HttpPost method = new HttpPost(url);
        //create groups
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        StringBuilder headerString = new StringBuilder();
        String responseStatus = "";
        ArrayList<String> subTests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            try {
                //create the group
                HttpEntity entity = new ByteArrayEntity(definedGroups.get(i).getBytes("UTF-8"));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    //obtain the schema corresponding to group
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

                    JSONDecoder jsonDecoder = new JSONDecoder();
                    Group group = null;
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
                    headerString.append(header.getName()).append(" : ").append(header.getValue()).append("\n");
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
     * @param groupId
     * @param testName
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
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 204) {
            return true;
        } else {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
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
        requestPath1.setTestCaseName("Get all groups");

        requestPaths = new RequestPath[]{requestPath1};

        for (int i = 0; i < requestPaths.length; i++) {
            HttpGet method = new HttpGet(url);

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
                //get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " " +
                        response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Read the response body.
                //get all headers
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString += header.getName() + " : " + header.getValue() + "\n";
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

               testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                        "Could not list the groups at url " + url,
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
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
                                    responseString, headerString, responseStatus, subTests);

                        } catch (BadRequestException | CharonException e) {

                           testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Response Validation Error",
                                    ComplianceUtils.getWire(method, responseString, headerString,
                                            responseStatus, subTests)));
                        }
                    }
                } catch (JSONException e) {

                    throw new ComplianceException(500, "Error in decoding the returned list resource.");

                } catch (BadRequestException | CharonException | InternalErrorException e) {

                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode the server response",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                // check for all created groups
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
                                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus,
                                            subTests)));
                        }
                    }

                } catch (CharonException e) {

                    throw new ComplianceException(500, "Could not get the created group id");
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
