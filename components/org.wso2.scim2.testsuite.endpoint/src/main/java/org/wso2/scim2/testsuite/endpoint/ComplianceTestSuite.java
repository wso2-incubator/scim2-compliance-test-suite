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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;

import org.wso2.scim2.testsuite.core.entities.Result;
import org.wso2.scim2.testsuite.core.entities.Statistics;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.pdf.PDFGenerator;
import org.wso2.scim2.testsuite.core.protocol.EndpointFactory;
import org.wso2.scim2.testsuite.core.tests.ResourceType;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Java servlet to serve API calls from React app.
 */
public class ComplianceTestSuite extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ComplianceTestSuite.class);

    public ComplianceTestSuite() {

        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h3>SCIM2 Compliance Test Suite</h3>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String endpoint;
        String userName;
        String password;
        String token;
        Boolean GetServiceProviderConfig = false;
        Boolean GetSchemas = false;
        Boolean GetResourceTypes = false;

        Boolean GetUsers = false;
        Boolean GetUserById = false;
        Boolean PostUser = false;
        Boolean PutUser = false;
        Boolean PatchUser = false;
        Boolean DeleteUser = false;
        Boolean SearchUser = false;

        Boolean GetGroups = false;
        Boolean GetGroupById = false;
        Boolean PostGroup = false;
        Boolean PutGroup = false;
        Boolean PatchGroup = false;
        Boolean DeleteGroup = false;
        Boolean SearchGroup = false;

        Boolean GetMe = false;
        Boolean PostMe = false;
        Boolean PutMe = false;
        Boolean PatchMe = false;
        Boolean DeleteMe = false;

        Boolean PostBulk = false;
        Boolean PutBulk = false;
        Boolean PatchBulk = false;
        Boolean DeleteBulk = false;

        try {
            ObjectMapper requestMapper = new ObjectMapper();
            //JSONObject jsonObject =  HTTP.toJSONObject(jb.toString());
            Map<String, Object> map = requestMapper.readValue(request.getInputStream(),
                    new TypeReference<Map<String, Object>>() {
                    });
            endpoint = (String) map.get("endpoint"); // will return price value.
            userName = (String) map.get("userName");
            password = (String) map.get("password");
            token = (String) map.get("token");
            GetServiceProviderConfig = (Boolean) map.get("GetServiceProviderConfig");
            GetSchemas = (Boolean) map.get("GetSchemas");
            GetResourceTypes = (Boolean) map.get("GetResourceTypes");

            GetUsers = (Boolean) map.get("GetUsers");
            GetUserById = (Boolean) map.get("GetUserById");
            PostUser = (Boolean) map.get("PostUser");
            PutUser = (Boolean) map.get("PutUser");
            PatchUser = (Boolean) map.get("PatchUser");
            DeleteUser = (Boolean) map.get("DeleteUser");
            SearchUser = (Boolean) map.get("SearchUser");

            GetGroups = (Boolean) map.get("GetGroups");
            GetGroupById = (Boolean) map.get("GetGroupById");
            PostGroup = (Boolean) map.get("PostGroup");
            PutGroup = (Boolean) map.get("PutGroup");
            PatchGroup = (Boolean) map.get("PatchGroup");
            DeleteGroup = (Boolean) map.get("DeleteGroup");
            SearchGroup = (Boolean) map.get("SearchGroup");

            GetMe = (Boolean) map.get("GetMe");
            PostMe = (Boolean) map.get("PostMe");
            PutMe = (Boolean) map.get("PutMe");
            PatchMe = (Boolean) map.get("PatchMe");
            DeleteMe = (Boolean) map.get("DeleteMe");

            PostBulk = (Boolean) map.get("PostBulk");
            PutBulk = (Boolean) map.get("PutBulk");
            PatchBulk = (Boolean) map.get("PatchBulk");
            DeleteBulk = (Boolean) map.get("DeleteBulk");

        } catch (JSONException e) {
            throw new IOException("Error parsing JSON request string");
        }

        EndpointFactory endFactory = new EndpointFactory(endpoint, userName, password, token);
        ResourceType resourceType = endFactory.getInstance("user");
        ResourceType resourceType2 = endFactory.getInstance("group");
        ResourceType resourceType3 = endFactory.getInstance("serviceProviderConfig");
        ResourceType resourceType4 = endFactory.getInstance("resourceType");
        ResourceType resourceType5 = endFactory.getInstance("schemaTest");
        ResourceType resourceType6 = endFactory.getInstance("me");
        ResourceType resourceType7 = endFactory.getInstance("bulk");
        ResourceType resourceType8 = endFactory.getInstance("role");

        Result finalResults = null;

        try {
            ArrayList<TestResult> results = new ArrayList<TestResult>();

            // Invoke ServiceProviderConfig test.
            if (GetServiceProviderConfig) {
                ArrayList<TestResult> serviceProviderResult;
                serviceProviderResult = resourceType3.getMethodTest();
                for (TestResult testResult : serviceProviderResult) {
                    results.add(testResult);
                }
            }

            // Invoke ResourceTypes test.
            if (GetResourceTypes) {
                ArrayList<TestResult> resourceTypeResult;
                resourceTypeResult = resourceType4.getMethodTest();
                for (TestResult testResult : resourceTypeResult) {
                    results.add(testResult);
                }
            }

            // Invoke schemas test.
            if (GetSchemas) {
                ArrayList<TestResult> schemaTestResult;
                schemaTestResult = resourceType5.getMethodTest();
                for (TestResult testResult : schemaTestResult) {
                    results.add(testResult);
                }
            }

            //  Invoke user related tests.
            if (GetUsers) {
                ArrayList<TestResult> userGetResult;
                userGetResult = resourceType.getMethodTest();
                for (TestResult testResult : userGetResult) {
                    results.add(testResult);
                }
            }
            if (PostUser) {
                ArrayList<TestResult> userPostResult;
                userPostResult = resourceType.postMethodTest();
                for (TestResult testResult : userPostResult) {
                    results.add(testResult);
                }
            }
            if (PatchUser) {
                ArrayList<TestResult> userPatchResult;
                userPatchResult = resourceType.patchMethodTest();
                for (TestResult testResult : userPatchResult) {
                    results.add(testResult);
                }
            }
            if (SearchUser) {
                ArrayList<TestResult> userSearchResult;
                userSearchResult = resourceType.searchMethodTest();
                for (TestResult testResult : userSearchResult) {
                    results.add(testResult);
                }
            }
            if (PutUser) {
                ArrayList<TestResult> userPutResult;
                userPutResult = resourceType.putMethodTest();
                for (TestResult testResult : userPutResult) {
                    results.add(testResult);
                }
            }
            if (DeleteUser) {
                ArrayList<TestResult> userDeleteResult;
                userDeleteResult = resourceType.deleteMethodTest();
                for (TestResult testResult : userDeleteResult) {
                    results.add(testResult);
                }
            }
            if (GetUserById) {
                ArrayList<TestResult> userGetByIDResult;
                userGetByIDResult = resourceType.getByIdMethodTest();
                for (TestResult testResult : userGetByIDResult) {
                    results.add(testResult);
                }
            }

            // Invoke group related tests.
            if (GetGroups) {
                ArrayList<TestResult> groupGetResult;
                groupGetResult = resourceType2.getMethodTest();
                for (TestResult testResult : groupGetResult) {
                    results.add(testResult);
                }
            }
            if (PostGroup) {
                ArrayList<TestResult> groupPostResult;
                groupPostResult = resourceType2.postMethodTest();
                for (TestResult testResult : groupPostResult) {
                    results.add(testResult);
                }
            }
            if (PatchGroup) {
                ArrayList<TestResult> groupPatchResult;
                groupPatchResult = resourceType2.patchMethodTest();
                for (TestResult testResult : groupPatchResult) {
                    results.add(testResult);
                }
            }
            if (SearchGroup) {
                ArrayList<TestResult> groupSearchResult;
                groupSearchResult = resourceType2.searchMethodTest();
                for (TestResult testResult : groupSearchResult) {
                    results.add(testResult);
                }
            }
            if (PutGroup) {
                ArrayList<TestResult> groupPutResult;
                groupPutResult = resourceType2.putMethodTest();
                for (TestResult testResult : groupPutResult) {
                    results.add(testResult);
                }
            }
            if (DeleteGroup) {
                ArrayList<TestResult> groupDeleteResult;
                groupDeleteResult = resourceType2.deleteMethodTest();
                for (TestResult testResult : groupDeleteResult) {
                    results.add(testResult);
                }
            }
            if (GetGroupById) {
                ArrayList<TestResult> groupGetByIDResult;
                groupGetByIDResult = resourceType2.getByIdMethodTest();
                for (TestResult testResult : groupGetByIDResult) {
                    results.add(testResult);
                }
            }

            // Invoke Me related tests.
            if (GetMe) {
                ArrayList<TestResult> meGetResult;
                meGetResult = resourceType6.getMethodTest();
                for (TestResult testResult : meGetResult) {
                    results.add(testResult);
                }
            }
            if (PostMe) {
                ArrayList<TestResult> mePostResult;
                mePostResult = resourceType6.postMethodTest();
                for (TestResult testResult : mePostResult) {
                    results.add(testResult);
                }
            }
            if (PatchMe) {
                ArrayList<TestResult> mePatchResult;
                mePatchResult = resourceType6.patchMethodTest();
                for (TestResult testResult : mePatchResult) {
                    results.add(testResult);
                }
            }
            if (PutMe) {
                ArrayList<TestResult> mePutResult;
                mePutResult = resourceType6.putMethodTest();
                for (TestResult testResult : mePutResult) {
                    results.add(testResult);
                }
            }
            if (DeleteMe) {
                ArrayList<TestResult> meDeleteResult;
                meDeleteResult = resourceType6.deleteMethodTest();
                for (TestResult testResult : meDeleteResult) {
                    results.add(testResult);
                }
            }

            // Invoke Bulk related tests.
            if (PostBulk) {
                ArrayList<TestResult> bulkPostResult;
                bulkPostResult = resourceType7.postMethodTest();
                for (TestResult testResult : bulkPostResult) {
                    results.add(testResult);
                }
            }
            if (PatchBulk) {
                ArrayList<TestResult> bulkPatchResult;
                bulkPatchResult = resourceType7.patchMethodTest();
                for (TestResult testResult : bulkPatchResult) {
                    results.add(testResult);
                }
            }
            if (PutBulk) {
                ArrayList<TestResult> bulkPutResult;
                bulkPutResult = resourceType7.putMethodTest();
                for (TestResult testResult : bulkPutResult) {
                    results.add(testResult);
                }
            }
            if (DeleteBulk) {
                ArrayList<TestResult> bulkDeleteResult;
                bulkDeleteResult = resourceType7.deleteMethodTest();
                for (TestResult testResult : bulkDeleteResult) {
                    results.add(testResult);
                }
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

            // Generate pdf results sheet.
//            try {
//                String fullPath = "/home/anjanap/Desktop/SCIM2\n";
//                String reportURL = PDFGenerator.generatePdfResults(finalResults, fullPath);
//                //TODO : Change this on server
//                finalResults.setReportLink("file://" + reportURL);
//            } catch (IOException pdf) {
//                new Result(pdf.getMessage());
//            }

        } catch (Exception ee) {
            new Result(ee.getMessage());
        }
        response.setContentType("application/json");
        // Get the printwriter object from response to write the required json object to the output stream.
        PrintWriter out = response.getWriter();

        // Map java object  to a json.
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(finalResults);

        // Assuming your json object is **jsonObject**, perform the following, it will return your json object.
        out.print(json);

    }
}
