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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This class consists of test cases related to /Me endpoint.
 */
public class MeTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;

    /**
     * Initialize.
     * @param complianceTestMetaDataHolder
     */
    public MeTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {
        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
        url =  complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.ME_ENDPOINT;
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
                    if(method.getName().equals("PatchUserTest")){
                        if (complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported()){
                            testResults.add((TestResult) method.invoke(this));
                        }else {
                            testResults.add(new TestResult(TestResult.SKIPPED,
                                    "Patch Me Test", "Skipped",null));

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
                        throw new ComplianceException("Error occurred in Me Test.");
                    }
                } catch (IllegalAccessException e) {
                    throw new ComplianceException("Error occurred in Me Test.");
                } catch (CharonException e) {
                    throw new ComplianceException("Error occurred in Me Test.");
                }

            }
        }
        return testResults;
    }

    /**
     * Create Me test case.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult CreateMeTest () throws GeneralComplianceException, ComplianceException {

        User user = null;
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
                try {
                    CleanUpUser(user.getId(), "Create Me");
                } catch (CharonException e1) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                            "Could not retrieve the user id",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema,null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                try {
                    CleanUpUser(user.getId(),"Create Me");
                } catch (CharonException e1) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                            "Could not retrieve the user id",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                CleanUpUser(user.getId(),"Create Me");
            } catch (CharonException e1) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create ME",
                        "Could not retrieve the user id",
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

    /**
     * Get me test case.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult GetMeTest () throws GeneralComplianceException, ComplianceException {

        User user = null;
        String id = InitiateUser("Get Me");
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
            CleanUpUser(id, "Get Me");
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
                CleanUpUser(id,"Get Me");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                CleanUpUser(id,"Get Me");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            CleanUpUser(id,"Get Me");
            return new TestResult
                    (TestResult.SUCCESS, "Get Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpUser(id,"Get Me");
            return new TestResult
                    (TestResult.ERROR, "Get Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * Update me test case.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult UpdateMeTest () throws GeneralComplianceException, ComplianceException {

        User user = null;
        String id = InitiateUser("Update Me");
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
            CleanUpUser(id,"Update Me");
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
                CleanUpUser(id,"Update Me");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                CleanUpUser(id,"Update Me");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            CleanUpUser(id,"Update Me");
            return new TestResult
                    (TestResult.SUCCESS, "Update Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpUser(id,"Update Me");
            return new TestResult
                    (TestResult.ERROR, "Update Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * Patch me test case.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult PatchMeTest () throws GeneralComplianceException, ComplianceException {

        User user = null;
        String id  = InitiateUser("Patch Me");
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
            CleanUpUser(id,"Patch Me");
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
                CleanUpUser(id,"Patch Me");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Me",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                CleanUpUser(id,"Patch Me");
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Me",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            CleanUpUser(id,"Patch Me");
            return new TestResult
                    (TestResult.SUCCESS, "Patch Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            CleanUpUser(id,"Patch Me");
            return new TestResult
                    (TestResult.ERROR, "Patch Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    /**
     * Delete me test case.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult DeleteMeTest () throws GeneralComplianceException, ComplianceException {

        User user = null;
        String id  = InitiateUser("Delete Me");
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
            CleanUpUser(id,"Delete Me");
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
            CleanUpUser(id,"Delete Me");
            return new TestResult
                    (TestResult.ERROR, "Delete Me",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }


    /**
     * This method cleans up the created used with the given id.
     * @param id
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    public boolean CleanUpUser (String id, String testName) throws GeneralComplianceException, ComplianceException {
        String userEndpointURL =  complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;
        String deleteUserURL = userEndpointURL + "/" + id;

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
     * This method creates a user and return its id.
     * @return
     * @throws GeneralComplianceException
     * @throws ComplianceException
     */
    public String InitiateUser (String testName) throws GeneralComplianceException, ComplianceException {

        User user = null;
        String userEndpointURL =  complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.USERS_ENDPOINT;
        HttpPost method = new HttpPost(userEndpointURL);
        //create user test
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
                user = (User)jsonDecoder.decodeResource(responseString, schema, new User());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(user, schema,null,
                        null, method,
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
}