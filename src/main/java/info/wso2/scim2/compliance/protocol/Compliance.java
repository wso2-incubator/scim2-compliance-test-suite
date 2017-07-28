package info.wso2.scim2.compliance.protocol;

import info.wso2.scim2.compliance.entities.Result;
import info.wso2.scim2.compliance.entities.Statistics;
import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.CriticalComplianceException;
import info.wso2.scim2.compliance.exception.GeneralComplianceException;
import info.wso2.scim2.compliance.pdf.PDFGenerator;
import info.wso2.scim2.compliance.tests.*;
import info.wso2.scim2.compliance.utils.ComplianceConstants;
import org.apache.commons.validator.routines.UrlValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;

@Path("/test2")
public class Compliance extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Result runTests(@FormParam(ComplianceConstants.RequestCodeConstants.URL) String url,
                           @FormParam(ComplianceConstants.RequestCodeConstants.USERNAME) String username,
                           @FormParam(ComplianceConstants.RequestCodeConstants.PASSWORD) String password,
                           @FormParam(ComplianceConstants.RequestCodeConstants.CLIENT_ID) String clientId,
                           @FormParam(ComplianceConstants.RequestCodeConstants.CLIENT_SECRET)
                                       String clientSecret,
                           @FormParam(ComplianceConstants.RequestCodeConstants.AUTHORIZATION_SERVER)
                                       String authorizationServer,
                           @FormParam(ComplianceConstants.RequestCodeConstants.AUTHORIZATION_HEADER)
                                       String authorizationHeader,
                           @FormParam(ComplianceConstants.RequestCodeConstants.AUTHORIZATION_METHOD)
                                       String authMethod)
            throws InterruptedException, ServletException {
        // TODO: remove when done coding!
        if (url == null || url.isEmpty()) {
            url = "https://localhost:9443/";
        }

        // This is to keep the test results
        ArrayList<TestResult> results = new ArrayList<TestResult>();

        // Valid schemas
        String[] schemes = {ComplianceConstants.RequestCodeConstants.HTTP,
                ComplianceConstants.RequestCodeConstants.HTTPS};

        UrlValidator urlValidator = new UrlValidator(schemes);
        /*
        if (!urlValidator.isValid(url)) {

            ComplianceException BadRequestException = new ComplianceException();
            BadRequestException.setDetail("Invalid Service Provider URL.");
            BadRequestException.setStatus(ComplianceConstants.ResponseCodeConstants.CODE_BAD_REQUEST);
            results.add(new TestResult(TestResult.ERROR, BadRequestException));

            Statistics statistics = new Statistics();
            statistics.incFailed();

            return new Result(statistics, results);
        }
        */

        // create a complianceTestMetaDataHolder to use to hold the test suite configurations
        ComplianceTestMetaDataHolder complianceTestMetaDataHolder = new ComplianceTestMetaDataHolder();
        complianceTestMetaDataHolder.setUrl(url);
        complianceTestMetaDataHolder.setVersion("/scim2");
        complianceTestMetaDataHolder.setUsername(username);
        complianceTestMetaDataHolder.setPassword(password);
        complianceTestMetaDataHolder.setAuthorization_server(authorizationServer);
        complianceTestMetaDataHolder.setAuthorization_header(authorizationHeader);
        complianceTestMetaDataHolder.setAuthorization_method(authMethod);
        complianceTestMetaDataHolder.setClient_id(clientId);
        complianceTestMetaDataHolder.setClient_secret(clientSecret);

        try {
            /***************** Start of critical tests **************/
            // start with the critical tests (will throw exception and test will stop if fails)
            // 1. /ServiceProviderConfig Test
            // 2. /Schemas Test
            // 3. /ResourceType Test
            //TODO : Check the comments

            // /ServiceProviderConfig Test
            ConfigTest configTest = new ConfigTest(complianceTestMetaDataHolder);
            results.add(configTest.performTest());

            /***************** End of critical tests **************/
            //TODO : All the other test should come here

        } catch (CriticalComplianceException e) {
            // failing critical test
            results.add(e.getResult());
        } catch (ComplianceException e) {
            //TODO :This need to be displyed in UI
            return (new Result(e.getMessage()));
        }

        /***************** Start of SCIMUser tests **************/
        //SCIMUser Test
        UserTest userTest = new UserTest(complianceTestMetaDataHolder);
        ArrayList<TestResult> userTestResults = null;
        try {
            userTestResults = userTest.performTest();
        } catch (ComplianceException e) {
            return (new Result(e.getMessage()));
        }
        for (TestResult testResult : userTestResults) {
            results.add(testResult);
        }
        /***************** End of SCIMUser tests **************/


        /***************** Start of SCIMGroup tests **************/
        //SCIMGroup Test
        GroupTest groupTest = new GroupTest(complianceTestMetaDataHolder);
        ArrayList<TestResult> groupTestResults = null;
        try {
            groupTestResults = groupTest.performTest();
        } catch (ComplianceException e) {
            return (new Result(e.getMessage()));
        }
        for (TestResult testResult : groupTestResults) {
            results.add(testResult);
        }
        /***************** End of SCIMGroup tests **************/

        /***************** Start of other tests **************/

        // /ResourceType Test
        ResourceTypeTest resourceTypeTest = new ResourceTypeTest(complianceTestMetaDataHolder);
        try {
            results.add(resourceTypeTest.performTest());
        } catch (CriticalComplianceException e) {
            results.add(e.getResult());
        } catch (ComplianceException e) {
            //TODO :This need to be displyed in UI
            return (new Result(e.getMessage()));
        }
        //List Test
        ListTest listTest = new ListTest(complianceTestMetaDataHolder);
        ArrayList<TestResult> listTestResults = new ArrayList<>();
        try {
            listTestResults = listTest.performTest();
        } catch (ComplianceException e) {
            return (new Result(e.getMessage()));
        }
        for (TestResult testResult : listTestResults) {
            results.add(testResult);
        }

        // Filter Test
        FilterTest filterTest = new FilterTest(complianceTestMetaDataHolder);
        ArrayList<TestResult> filterTestResults = new ArrayList<>();
        try {
            filterTestResults = filterTest.performTest();
        } catch (ComplianceException e) {
            return (new Result(e.getMessage()));
        }
        for (TestResult testResult : filterTestResults) {
            results.add(testResult);
        }
        /***************** End of other tests **************/

        Statistics statistics = new Statistics();
        for (TestResult result : results) {

            switch (result.getStatus()) {
                case TestResult.ERROR:
                    statistics.incFailed();
                    break;
                case TestResult.SUCCESS:
                    statistics.incSuccess();
                    break;
                case TestResult.SKIPPED:
                    statistics.incSkipped();
                    break;
            }
        }
        Result finalResults = new Result(statistics, results);
        //generate pdf results sheet
        try {
            PDFGenerator.GeneratePDFResults(finalResults);
        } catch (IOException e) {
            return (new Result(e.getMessage()));
        }
        return finalResults;
    }
}
