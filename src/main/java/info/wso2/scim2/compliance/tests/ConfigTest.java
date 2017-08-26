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

import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.GeneralComplianceException;
import info.wso2.scim2.compliance.protocol.ComplianceUtils;
import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.CriticalComplianceException;
import info.wso2.scim2.compliance.httpclient.HTTPClient;
import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import info.wso2.scim2.compliance.objects.SCIMServiceProviderConfig;
import info.wso2.scim2.compliance.tests.common.ResponseValidateTests;
import info.wso2.scim2.compliance.utils.ComplianceConstants;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
* This Class is to test the /ServiceProviderConfig Endpoint.
 **/
public class ConfigTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private SCIMServiceProviderConfig scimServiceProviderConfig = null;

    /**
     * Initializer.
     * @param complianceTestMetaDataHolder
     */
    public ConfigTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {
        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
    }

    /**
     * Test is to get the service provider configurations from service provider.
     **/
    public ArrayList<TestResult> performTest() throws CriticalComplianceException, ComplianceException{
        ArrayList<TestResult> testResults = new ArrayList<>();
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            TestCase annos = method.getAnnotation(TestCase.class);
            if (annos != null) {
                try {
                    testResults.add((TestResult) method.invoke(this));
                } catch (InvocationTargetException e) {
                    try{
                        throw  e.getCause();
                    } catch (ComplianceException e1) {
                        throw e1;
                    } catch (CriticalComplianceException e1){
                        testResults.add(e1.getResult());
                    } catch (Throwable throwable) {
                        throw new ComplianceException("Error occurred in Config Test.");
                    }
                } catch (IllegalAccessException e) {
                    throw new ComplianceException("Error occurred in Config Test.");
                }

            }
        }
        return testResults;
    }

    /**
     * Method to get the service provider configs.
     * @return
     * @throws CriticalComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult getServiceProviderConfigTest () throws CriticalComplianceException, ComplianceException {
        // Construct the endpoint url
        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.SERVICE_PROVIDER_ENDPOINT;

        // specify the get request
        HttpGet method = new HttpGet(url);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();

        try {
            //get the service provider configs
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
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, "Get ServiceProviderConfig",
                            "Could not get ServiceProviderConfig at url " + url,
                            ComplianceUtils.getWire(method, responseString,
                                    headerString, responseStatus, subTests)));
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to serviceProviderConfig
            SCIMResourceTypeSchema schema = complianceTestMetaDataHolder.getScimSchema().
                    getServiceProviderConfigSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                scimServiceProviderConfig =
                        (SCIMServiceProviderConfig)
                                jsonDecoder.decodeResource(responseString, schema,
                                        new SCIMServiceProviderConfig());
                complianceTestMetaDataHolder.setScimServiceProviderConfig(scimServiceProviderConfig);

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new CriticalComplianceException(new TestResult(TestResult.ERROR, "Get ServiceProviderConfig",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(scimServiceProviderConfig, schema, null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new CriticalComplianceException(new TestResult(TestResult.ERROR, "Get ServiceProviderConfig",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            } catch (GeneralComplianceException e) {
                throw new CriticalComplianceException(new TestResult(TestResult.ERROR, "Get ServiceProviderConfig",
                        e.getResult().getMessage(), ComplianceUtils.getWire(method,
                        responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Get ServiceProviderConfig",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Get ServiceProviderConfig",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }
}
