package info.wso2.scim2.compliance.tests;

import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.CriticalComplianceException;
import info.wso2.scim2.compliance.httpclient.HTTPClient;
import info.wso2.scim2.compliance.objects.SCIMSchema;
import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import info.wso2.scim2.compliance.protocol.ComplianceUtils;
import info.wso2.scim2.compliance.utils.ComplianceConstants;
import info.wso2.scim2.compliance.utils.SchemaBuilder;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This class performs the /Schemas test.
 */
public class SchemaTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private SCIMSchema scimSchema = new SCIMSchema();
    /**
     * Initializer.
     * @param complianceTestMetaDataHolder
     */
    public SchemaTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {
        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
    }

    /**
     * Test is to get the service provider configurations from service provider.
     **/
    public ArrayList<TestResult> performTest() throws CriticalComplianceException, ComplianceException {
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
                        throw new ComplianceException("Error occurred in Schema Test.");
                    }
                } catch (IllegalAccessException e) {
                    throw new ComplianceException("Error occurred in Schema Test.");
                }

            }
        }
        return testResults;
    }

    /**
     * Method to get the schemas.
     * @return
     * @throws CriticalComplianceException
     * @throws ComplianceException
     */
    @TestCase
    public TestResult getSchemasTest() throws CriticalComplianceException, ComplianceException {

        // set the scim schema object
        complianceTestMetaDataHolder.setScimSchema(scimSchema);

        // Construct the endpoint url
        String url = complianceTestMetaDataHolder.getUrl() +
                ComplianceConstants.TestConstants.SCHEMAS_ENDPOINT;
        // specify the get request
        HttpGet method = new HttpGet(url);

        HttpClient client = HTTPClient.getHttpClient();

        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();

        try {
            //get the schemas
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
                    (TestResult.ERROR, "Get Schemas",
                            "Could not get Schemas at url " + url,
                            ComplianceUtils.getWire(method, responseString,
                                    headerString, responseStatus, subTests)));
        }
        if (response.getStatusLine().getStatusCode() == 200) {

            //build the schemas according to service provider.
            SchemaBuilder.buildSchema(responseString, method, headerString, responseStatus,
                    subTests, url, scimSchema);

            return new TestResult
                    (TestResult.SUCCESS, "Get Schemas",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Get Schemas",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }
}
