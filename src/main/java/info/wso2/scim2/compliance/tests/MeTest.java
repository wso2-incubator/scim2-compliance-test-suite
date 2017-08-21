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
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.util.ArrayList;

public class MeTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;
    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MeTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url =  complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.ME_ENDPOINT;
    }

    public ArrayList<TestResult> performTest() throws ComplianceException {
        ArrayList<TestResult> testResults = new ArrayList<>();
        try {
            //perform create user test
            testResults.add(CreateUserTest());
            //perform get user test
            testResults.add(GetUserTest());
            //perform update user test
            testResults.add(UpdateUserTest());

            if (complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported()){
                //perform patch user test if and only if it is supported by the SCIM service provider
                testResults.add(PatchUserTest());
            } else {
                testResults.add(new TestResult(TestResult.SKIPPED, "Patch User Test", "Skipped",null));
            }
            //perform delete user test
            testResults.add(DeleteUserTest());

        } catch (GeneralComplianceException e) {
            testResults.add(e.getResult());
        } catch (CharonException e) {
           throw  new ComplianceException(500, "Error in getting the Patch attribute");
        }
        return testResults;
    }

    public TestResult CreateUserTest () throws GeneralComplianceException, ComplianceException {

        HttpPost method = new HttpPost(url);
        //create user test
        HttpClient client = HTTPClient.getHttpClient();

        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
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
            responseStatus = response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            // Read the response body.
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                    "Could not create default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 201) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                user = (User)jsonDecoder.decodeResource(responseString, schema, new User());
            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema,null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Create Me",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Create Me",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests));
        }
    }

    public TestResult GetUserTest () throws GeneralComplianceException, ComplianceException {

        HttpGet method = new HttpGet(url);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpGet) HTTPClient.setAuthorizationHeader(
                ComplianceConstants.DefinedInstances.DEFINED_USER_USERNAME,
                ComplianceConstants.DefinedInstances.DEFINED_USER_PASSWORD,
                method);

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
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Me",
                    "Could not get the default user from url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                user = (User)jsonDecoder.decodeResource(responseString, schema, new User());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Get Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Get Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    public TestResult UpdateUserTest () throws GeneralComplianceException, ComplianceException {

        HttpPut method = new HttpPut(url);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPut) HTTPClient.setAuthorizationHeader(
                ComplianceConstants.DefinedInstances.DEFINED_USER_USERNAME,
                ComplianceConstants.DefinedInstances.DEFINED_USER_PASSWORD,
                method);

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //update the user
            HttpEntity entity = new ByteArrayEntity
                    (ComplianceConstants.DefinedInstances.DEFINED_UPDATED_USER.getBytes("UTF-8"));
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
            // Read the response body.
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Me",
                    "Could not update the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                user = (User)jsonDecoder.decodeResource(responseString, schema, new User());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Update Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Update Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    public TestResult PatchUserTest () throws GeneralComplianceException, ComplianceException {

        HttpPatch method = new HttpPatch(url);
        //create user test
        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpPatch) HTTPClient.setAuthorizationHeader(
                ComplianceConstants.DefinedInstances.DEFINED_USER_USERNAME,
                ComplianceConstants.DefinedInstances.DEFINED_USER_PASSWORD,
                method);

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //patch the user
            HttpEntity entity = new ByteArrayEntity
                    (ComplianceConstants.DefinedInstances.DEFINED_PATCH_USER_PAYLOAD.getBytes("UTF-8"));
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
            // Read the response body.
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Me",
                    "Could not patch the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                user = (User)jsonDecoder.decodeResource(responseString, schema, new User());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Patch Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Patch Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    public TestResult DeleteUserTest () throws GeneralComplianceException, ComplianceException {

        HttpDelete method = new HttpDelete(url);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpDelete) HTTPClient.setAuthorizationHeader(
                ComplianceConstants.DefinedInstances.DEFINED_USER_USERNAME,
                ComplianceConstants.DefinedInstances.DEFINED_USER_PASSWORD,
                method);

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
            CleanUpDelete();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Delete Me",
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 204) {
            return new TestResult
                    (TestResult.SUCCESS, "Delete Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpDelete();
            return new TestResult
                    (TestResult.ERROR, "Delete Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }


    public TestResult CleanUpDelete () throws GeneralComplianceException, ComplianceException {

        String deleteUserURL = null;
        try {
            deleteUserURL = complianceTestMetaDataHolder.getUrl() +
                    ComplianceConstants.TestConstants.USERS_ENDPOINT + "/" + user.getId();
        } catch (CharonException | NullPointerException e) {
            throw new ComplianceException("Error in reading the id of the created user.");
        }
        HttpDelete method = new HttpDelete(deleteUserURL);

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
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Delete Me",
                    "Could not clean up the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 204) {
            return new TestResult
                    (TestResult.SUCCESS, "Delete Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Delete Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }
}

