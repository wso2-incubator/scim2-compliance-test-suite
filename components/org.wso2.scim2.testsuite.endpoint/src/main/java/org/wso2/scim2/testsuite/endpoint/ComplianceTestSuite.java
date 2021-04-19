/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.scim2.testsuite.endpoint;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;

import org.wso2.scim2.testsuite.core.entities.Result;
import org.wso2.scim2.testsuite.core.entities.Statistics;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.pdf.PDFGenerator;
import org.wso2.scim2.testsuite.core.protocol.EndpointFactory;
import org.wso2.scim2.testsuite.core.tests.ResourceType;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Java servlet to serve API calls from React app.
 */
public class ComplianceTestSuite extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public ComplianceTestSuite() {

        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Map request json data to invoke tests.
        ObjectMapper requestMapper = new ObjectMapper();
        Map<String, Object> map = requestMapper.readValue(request.getInputStream(),
                new TypeReference<Map<String, Object>>() {
                });
        String endpoint = (String) map.get("endpoint");
        String userName = (String) map.get("userName");
        String password = (String) map.get("password");
        String token = (String) map.get("token");
        Boolean getServiceProviderConfig = (Boolean) map.get("getServiceProviderConfig");
        Boolean getSchemas = (Boolean) map.get("getSchemas");
        Boolean getResourceTypes = (Boolean) map.get("getResourceTypes");

        Boolean getUsers = (Boolean) map.get("getUsers");
        Boolean getUserById = (Boolean) map.get("getUserById");
        Boolean postUser = (Boolean) map.get("postUser");
        Boolean putUser = (Boolean) map.get("putUser");
        Boolean patchUser = (Boolean) map.get("patchUser");
        Boolean deleteUser = (Boolean) map.get("deleteUser");
        Boolean searchUser = (Boolean) map.get("searchUser");

        Boolean getGroups = (Boolean) map.get("getGroups");
        Boolean getGroupById = (Boolean) map.get("getGroupById");
        Boolean postGroup = (Boolean) map.get("postGroup");
        Boolean putGroup = (Boolean) map.get("putGroup");
        Boolean patchGroup = (Boolean) map.get("patchGroup");
        Boolean deleteGroup = (Boolean) map.get("deleteGroup");
        Boolean searchGroup = (Boolean) map.get("searchGroup");

        Boolean getMe = (Boolean) map.get("getMe");
        Boolean postMe = (Boolean) map.get("postMe");
        Boolean putMe = (Boolean) map.get("putMe");
        Boolean patchMe = (Boolean) map.get("patchMe");
        Boolean deleteMe = (Boolean) map.get("deleteMe");

        Boolean postBulk = (Boolean) map.get("postBulk");
        Boolean putBulk = (Boolean) map.get("putBulk");
        Boolean patchBulk = (Boolean) map.get("patchBulk");
        Boolean deleteBulk = (Boolean) map.get("deleteBulk");

        // Invoke test library by providing authentication data.
        EndpointFactory endFactory = new EndpointFactory(endpoint, userName, password, token);
        ResourceType user = endFactory.getInstance(ComplianceConstants.EndPointConstants.USER);
        ResourceType group = endFactory.getInstance(ComplianceConstants.EndPointConstants.GROUP);
        ResourceType serviceProviderConfig =
                endFactory.getInstance(ComplianceConstants.EndPointConstants.SERVICEPROVIDERCONFIG);
        ResourceType resourceType = endFactory.getInstance(ComplianceConstants.EndPointConstants.RESOURCETYPE);
        ResourceType schema = endFactory.getInstance(ComplianceConstants.EndPointConstants.SCHEMAS);
        ResourceType self = endFactory.getInstance(ComplianceConstants.EndPointConstants.ME);
        ResourceType bulk = endFactory.getInstance(ComplianceConstants.EndPointConstants.BULK);

        Result finalResults = null;

        try {
            ArrayList<TestResult> results = new ArrayList<>();

            // Get service provider config to check which tests are compatible with service provider.
            serviceProviderConfig.getMethodTest();

            // Invoke ServiceProviderConfig test.
            if (getServiceProviderConfig) {
                ArrayList<TestResult> serviceProviderResult;
                serviceProviderResult = serviceProviderConfig.getMethodTest();
                results.addAll(serviceProviderResult);
            }

            // Invoke ResourceTypes test.
            if (getResourceTypes) {
                ArrayList<TestResult> resourceTypeResult;
                resourceTypeResult = resourceType.getMethodTest();
                results.addAll(resourceTypeResult);
            }

            // Invoke schemas test.
            if (getSchemas) {
                ArrayList<TestResult> schemaTestResult;
                schemaTestResult = schema.getMethodTest();
                results.addAll(schemaTestResult);
            }

            //  Invoke user related tests.
            if (getUsers) {
                ArrayList<TestResult> userGetResult;
                userGetResult = user.getMethodTest();
                results.addAll(userGetResult);
            }
            if (postUser) {
                ArrayList<TestResult> userPostResult;
                userPostResult = user.postMethodTest();
                results.addAll(userPostResult);
            }
            if (patchUser) {
                ArrayList<TestResult> userPatchResult;
                userPatchResult = user.patchMethodTest();
                results.addAll(userPatchResult);
            }
            if (searchUser) {
                ArrayList<TestResult> userSearchResult;
                userSearchResult = user.searchMethodTest();
                results.addAll(userSearchResult);
            }
            if (putUser) {
                ArrayList<TestResult> userPutResult;
                userPutResult = user.putMethodTest();
                results.addAll(userPutResult);
            }
            if (deleteUser) {
                ArrayList<TestResult> userDeleteResult;
                userDeleteResult = user.deleteMethodTest();
                results.addAll(userDeleteResult);
            }
            if (getUserById) {
                ArrayList<TestResult> userGetByIDResult;
                userGetByIDResult = user.getByIdMethodTest();
                results.addAll(userGetByIDResult);
            }

            // Invoke group related tests.
            if (getGroups) {
                ArrayList<TestResult> groupGetResult;
                groupGetResult = group.getMethodTest();
                results.addAll(groupGetResult);
            }
            if (postGroup) {
                ArrayList<TestResult> groupPostResult;
                groupPostResult = group.postMethodTest();
                results.addAll(groupPostResult);
            }
            if (patchGroup) {
                ArrayList<TestResult> groupPatchResult;
                groupPatchResult = group.patchMethodTest();
                results.addAll(groupPatchResult);
            }
            if (searchGroup) {
                ArrayList<TestResult> groupSearchResult;
                groupSearchResult = group.searchMethodTest();
                results.addAll(groupSearchResult);
            }
            if (putGroup) {
                ArrayList<TestResult> groupPutResult;
                groupPutResult = group.putMethodTest();
                results.addAll(groupPutResult);
            }
            if (deleteGroup) {
                ArrayList<TestResult> groupDeleteResult;
                groupDeleteResult = group.deleteMethodTest();
                results.addAll(groupDeleteResult);
            }
            if (getGroupById) {
                ArrayList<TestResult> groupGetByIDResult;
                groupGetByIDResult = group.getByIdMethodTest();
                results.addAll(groupGetByIDResult);
            }

            // Invoke Me related tests.
            if (getMe) {
                ArrayList<TestResult> meGetResult;
                meGetResult = self.getMethodTest();
                results.addAll(meGetResult);
            }
            if (postMe) {
                ArrayList<TestResult> mePostResult;
                mePostResult = self.postMethodTest();
                results.addAll(mePostResult);
            }
            if (patchMe) {
                ArrayList<TestResult> mePatchResult;
                mePatchResult = self.patchMethodTest();
                results.addAll(mePatchResult);
            }
            if (putMe) {
                ArrayList<TestResult> mePutResult;
                mePutResult = self.putMethodTest();
                results.addAll(mePutResult);
            }
            if (deleteMe) {
                ArrayList<TestResult> meDeleteResult;
                meDeleteResult = self.deleteMethodTest();
                results.addAll(meDeleteResult);
            }

            // Invoke Bulk related tests.
            if (postBulk) {
                ArrayList<TestResult> bulkPostResult;
                bulkPostResult = bulk.postMethodTest();
                results.addAll(bulkPostResult);
            }
            if (patchBulk) {
                ArrayList<TestResult> bulkPatchResult;
                bulkPatchResult = bulk.patchMethodTest();
                results.addAll(bulkPatchResult);
            }
            if (putBulk) {
                ArrayList<TestResult> bulkPutResult;
                bulkPutResult = bulk.putMethodTest();
                results.addAll(bulkPutResult);
            }
            if (deleteBulk) {
                ArrayList<TestResult> bulkDeleteResult;
                bulkDeleteResult = bulk.deleteMethodTest();
                results.addAll(bulkDeleteResult);
            }

            // Calculate test statistics.
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
            long time = 0;
            for (TestResult result : results) {
                time += result.getElapsedTime();
            }
            statistics.setTime(time);

            finalResults = new Result(statistics, results);

        } catch (Exception e) {
            new Result(e.getMessage());
        }

        // Generate pdf results sheet
        try {
            ServletContext context = request.getServletContext();
            String fullPath = context.getRealPath("/");
            String savePath = fullPath + "/WEB-INF/SCIM 2.0 Compliance Test Suite - Auto Generated Test Report";
            String reportURL = PDFGenerator.generatePdfResults(finalResults, savePath);
            assert finalResults != null;
            finalResults.setReportLink("file://" + reportURL);
        } catch (IOException e) {
            new Result(e.getMessage());
        }

        response.setContentType("application/json");
        // Get the printWriter object from response to write the required json object to the output stream.
        PrintWriter out = response.getWriter();
        // Map java object  to a json.
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(finalResults);
        // Assuming your json object is **jsonObject**, perform the following, it will return your json object.
        out.print(json);
    }
}
