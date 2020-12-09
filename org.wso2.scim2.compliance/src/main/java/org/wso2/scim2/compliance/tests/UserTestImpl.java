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
import org.wso2.scim2.compliance.utils.ComplianceConstants;

import java.util.ArrayList;

/**
 * Implemetation of  for User test cases.
 */
public class UserTestImpl implements ResourceType {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;

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
     * This method creates a user and return its id.
     *
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    private String initiateUser(String testName) throws GeneralComplianceException, ComplianceException {

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
            //create the user
            HttpEntity entity = new ByteArrayEntity
                    (ComplianceConstants.DefinedInstances.DEFINED_USER.getBytes("UTF-8"));
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
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema
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

    @Override
    public TestResult getMethodTest() throws GeneralComplianceException, ComplianceException {
        //create default user;
        String id = initiateUser("Get User");
        User user = null;
        String getUserURL = url + "/" + id;

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
            //clean the created user
            cleanUpUser(id, "Get User");
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get User",
                    "Could not get the default user from url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                user = (User) jsonDecoder.decodeResource(responseString, schema, new User());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                //clean the created user
                cleanUpUser(id, "Get User");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get User",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                //clean the created user
                cleanUpUser(id, "Get User");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get User",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            //clean the created user
            cleanUpUser(id, "Get User");
            return new TestResult
                    (TestResult.SUCCESS, "Get User",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            //clean the created user
            cleanUpUser(id, "Get User");
            return new TestResult
                    (TestResult.ERROR, "Get User",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    @Override
    public TestResult postMethodTest() throws GeneralComplianceException, ComplianceException {

        try {
            System.out.println("Hello from useer");
            return null;

        } catch (Exception e) {
            System.out.println("error");
            System.out.println(e);
        }
        return null;
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
