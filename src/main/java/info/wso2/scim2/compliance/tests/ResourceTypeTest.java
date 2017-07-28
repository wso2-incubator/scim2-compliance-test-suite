package info.wso2.scim2.compliance.tests;


import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.CriticalComplianceException;
import info.wso2.scim2.compliance.exception.GeneralComplianceException;
import info.wso2.scim2.compliance.httpclient.HTTPClient;
import info.wso2.scim2.compliance.objects.SCIMResourceType;
import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import info.wso2.scim2.compliance.protocol.ComplianceUtils;
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
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.util.ArrayList;

public class ResourceTypeTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    SCIMResourceType scimResourceType = null;

    public ResourceTypeTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {
        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
    }

    // Test is to get the resource types from service provider
    public TestResult performTest() throws CriticalComplianceException, ComplianceException {
        return getResourceTypeTest();
    }

    private TestResult getResourceTypeTest () throws CriticalComplianceException, ComplianceException {
        // Construct the endpoint url
        String url = complianceTestMetaDataHolder.getUrl() +
                complianceTestMetaDataHolder.getVersion() +
                ComplianceConstants.TestConstants.RESOURCE_TYPE_ENDPOINT;

        // specify the get request
        HttpGet method = new HttpGet(url);

        HttpClient client = HTTPClient.getHttpClientWithBasicAuth();

        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();

        try {
            //get the resource types
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
                    (TestResult.ERROR, "Get ResourceType",
                            "Could not get ResourceType at url " + url,
                            ComplianceUtils.getWire(method, responseString,
                                    headerString, responseStatus, subTests)));
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to resourceType
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.
                    getInstance().getResourceTypeResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                scimResourceType =
                        (SCIMResourceType) jsonDecoder.decodeResource(responseString, schema,
                                        new SCIMResourceType());
                complianceTestMetaDataHolder.setScimResourceType(scimResourceType);

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new CriticalComplianceException(new TestResult(TestResult.ERROR,
                        "Get ResourceType",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(scimResourceType, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new CriticalComplianceException(new TestResult(TestResult.ERROR,
                        "Get ResourceType",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            } catch (GeneralComplianceException e) {
                throw new CriticalComplianceException(new TestResult(TestResult.ERROR,
                        "Get ResourceType",
                        e.getResult().getMessage(), ComplianceUtils.getWire(method,
                        responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Get ResourceType",
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
