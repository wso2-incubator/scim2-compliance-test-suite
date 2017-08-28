/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.wso2.scim2.compliance.tests;


import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.GeneralComplianceException;
import info.wso2.scim2.compliance.httpclient.HTTPClient;
import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import info.wso2.scim2.compliance.protocol.ComplianceUtils;
import info.wso2.scim2.compliance.tests.common.ResponseValidateTests;
import info.wso2.scim2.compliance.utils.ComplianceConstants;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class consists of test cases related to /Groups endpoint.
 */
public class GroupTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;

    /**
     * Initialize.
     * @param complianceTestMetaDataHolder
     */
    public GroupTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {
        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
        url =  complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.GROUPS_ENDPOINT;
    }

    /**
     * Method to handle test cases.
     * @return
     * @throws ComplianceException
     */
    public ArrayList<TestResult> performTest() throws ComplianceException {
        ArrayList<TestResult> testResults = new ArrayList<>();
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            TestCase annos = method.getAnnotation(TestCase.class);
            if (annos != null) {
                try {
                    if(method.getName().equals("PatchGroupTest")){
                        if (complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported()){
                            testResults.add((TestResult) method.invoke(this));
                        } else {
                            testResults.add(new TestResult(TestResult.SKIPPED,
                                    "Patch Group Test", "Skipped",null));
                        }
                    } else{
                        testResults.add((TestResult) method.invoke(this));
                    }
                } catch (InvocationTargetException e) {
                    try{
                        throw  e.getCause();
                    } catch (ComplianceException e1) {
                        throw e1;
                    } catch (GeneralComplianceException e1){
                        testResults.add(e1.getResult());
                    } catch (Throwable throwable) {
                        throw new ComplianceException("Error occurred in Group Test.");
                    }
                } catch (IllegalAccessException e) {
                    throw new ComplianceException("Error occurred in Group Test.");
                } catch (CharonException e) {
                    throw new ComplianceException("Error occurred in Group Test.");
                }

            }
        }
        return testResults;
    }

    /**
     * Group Create Test.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult CreateGroupTest () throws GeneralComplianceException, ComplianceException {

        Group group = null;
        String definedGroup = null;

        definedGroup = "{\"displayName\": \"engineer\"}";
        HttpPost method = new HttpPost(url);
        //create group test
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //create the group
            HttpEntity entity = new ByteArrayEntity(definedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
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
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Group",
                    "Could not create default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 201) {
            //obtain the schema corresponding to group
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group)jsonDecoder.decodeResource(responseString, schema, new Group());
            } catch (BadRequestException | CharonException | InternalErrorException e) {
                try {
                    CleanUpGroup(group.getId(), "Group Create");
                } catch (CharonException e1) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Group Create",
                            "Could not retrieve the group id",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema,null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                try {
                    CleanUpGroup(group.getId(), "Group Create");
                } catch (CharonException e1) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Group Create",
                            "Could not retrieve the group id",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                CleanUpGroup(group.getId(), "Group Create");
            } catch (CharonException e1) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Group Create",
                        "Could not retrieve the group id",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Create Group",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Create Group",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests));
        }
    }

    /**
     * Group get test.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult GetGroupTest () throws GeneralComplianceException, ComplianceException {

        String id = InitiateGroup("Get Group");
        Group group = null;
        String getGroupURL = null;

        getGroupURL = url + "/" + id;

        HttpGet method = new HttpGet(getGroupURL);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
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
            // get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            CleanUpGroup(id,"Get Group");
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Group",
                    "Could not get the default group from url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to group
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                CleanUpGroup(id,"Get Group");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                CleanUpGroup(id,"Get Group");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }

            CleanUpGroup(id,"Get Group");
            return new TestResult
                    (TestResult.SUCCESS, "Get Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpGroup(id,"Get Group");
            return new TestResult
                    (TestResult.ERROR, "Get Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * Group update test.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult UpdateGroupTest () throws GeneralComplianceException, ComplianceException {

        Group group = null;
        String id = InitiateGroup("Update Group");
        String updateUserURL = null;
        String definedUpdatedGroup = null;
        try {
            definedUpdatedGroup = "{\"displayName\": \"Doctors\"}";
        } catch (Exception e) {
            throw new ComplianceException("Error while getting the user to add to group");
        }

        updateUserURL = url + "/" + id;

        HttpPut method = new HttpPut(updateUserURL);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPut) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //update the user
            HttpEntity entity = new ByteArrayEntity(definedUpdatedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");

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
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            CleanUpGroup(id, "Update Group");
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Group",
                    "Could not update the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                CleanUpGroup(id, "Update Group");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {

                CleanUpGroup(id,"Update Group");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }

            CleanUpGroup(id, "Update Group");
            return new TestResult
                    (TestResult.SUCCESS, "Update Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpGroup(id, "Update Group");
            return new TestResult
                    (TestResult.ERROR, "Update Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * Group patch test.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult PatchGroupTest () throws GeneralComplianceException, ComplianceException {

        Group group = null;
        String id = InitiateGroup("Patch Group");
        String patchGroupURL = null;
        String definedPatchedGroup = null;
        definedPatchedGroup = "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
                "\"Operations\":[{\"op\":\"add\",\"value\":{\"displayName\": \"Actors\"}}]}";

        patchGroupURL = url + "/" + id;

        HttpPatch method = new HttpPatch(patchGroupURL);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPatch) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //update the user
            HttpEntity entity = new ByteArrayEntity(definedPatchedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");

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
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            CleanUpGroup(id, "Patch Group");
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Group",
                    "Could not patch the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                CleanUpGroup(id, "Patch Group");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                CleanUpGroup(id, "Patch Group");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            CleanUpGroup(id,"Patch Group");
            return new TestResult
                    (TestResult.SUCCESS, "Patch Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpGroup(id, "Patch Group");
            return new TestResult
                    (TestResult.ERROR, "Patch Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * Group delete test.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult DeleteGroupTest () throws GeneralComplianceException, ComplianceException {

        Group group = null;
        String id = InitiateGroup("Delete Group");
        String deleteGroupURL = null;

        deleteGroupURL = url + "/" + id;

        HttpDelete method = new HttpDelete(deleteGroupURL);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpDelete) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
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
            CleanUpGroup(id, "Delete Group");
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Delete Group",
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 204) {
            return new TestResult
                    (TestResult.SUCCESS, "Delete Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpGroup(id, "Delete Group");
            return new TestResult
                    (TestResult.ERROR, "Delete Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * This method cleans the group with the given groupId and the user with the given id.
     * @param groupId
     * @param testName
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    public boolean CleanUpGroup (String groupId, String testName)
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
        ArrayList<String> subTests =  new ArrayList<>();
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

    /**
     * This method created a group and return the id.
     * @param testName
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    public String InitiateGroup(String testName)
            throws GeneralComplianceException, ComplianceException {

        Group group = null;
        String definedGroup = null;

        definedGroup = "{\"displayName\": \"YERFTERI\"}";
        HttpPost method = new HttpPost(url);
        //create group test
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //create the group
            HttpEntity entity = new ByteArrayEntity(definedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
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
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not create default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 201) {
            //obtain the schema corresponding to group
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group)jsonDecoder.decodeResource(responseString, schema, new Group());
            } catch (BadRequestException | CharonException | InternalErrorException e) {

                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema,null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {

                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                return group.getId();
            } catch (CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "Could not retrieve the group id",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        } else {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not create default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
    }
}
