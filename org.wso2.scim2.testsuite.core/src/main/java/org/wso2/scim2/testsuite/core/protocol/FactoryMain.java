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

package org.wso2.scim2.testsuite.core.protocol;

//import org.apache.commons.lang.StringUtils;
import org.wso2.scim2.testsuite.core.entities.Result;
import org.wso2.scim2.testsuite.core.entities.Statistics;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.pdf.PDFGenerator;
import org.wso2.scim2.testsuite.core.tests.ResourceType;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * Method for calling factory.
 */
public class FactoryMain {

    @Context
    static
    ServletContext context;

    public static void main(String a[]) {

        EndpointFactory endFactory = new EndpointFactory();
        ResourceType resourceType = endFactory.getInstance("user");
        ResourceType resourceType2 = endFactory.getInstance("group");
        ResourceType resourceType3 = endFactory.getInstance("serviceProviderConfig");
        ResourceType resourceType4 = endFactory.getInstance("resourceType");
        ResourceType resourceType5 = endFactory.getInstance("schemaTest");
        ResourceType resourceType6 = endFactory.getInstance("me");
        ResourceType resourceType7 = endFactory.getInstance("bulk");
        ResourceType resourceType8 = endFactory.getInstance("role");
        try {
            ArrayList<TestResult> results = new ArrayList<TestResult>();

            // ServiceProviderConfig
            ArrayList<TestResult> serviceProviderResult;
            serviceProviderResult = resourceType3.getMethodTest();
            for (TestResult testResult : serviceProviderResult) {
                results.add(testResult);
            }

            // resourceType
            ArrayList<TestResult> resourceTypeResult;
            resourceTypeResult = resourceType4.getMethodTest();
            for (TestResult testResult : resourceTypeResult) {
                results.add(testResult);
            }

            // schemaTest
            ArrayList<TestResult> schemaTestResult;
            schemaTestResult = resourceType5.getMethodTest();
            for (TestResult testResult : schemaTestResult) {
                results.add(testResult);
            }

            //  User
            ArrayList<TestResult> userGetResult;
            userGetResult = resourceType.getMethodTest();
            for (TestResult testResult : userGetResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userPostResult;
            userPostResult = resourceType.postMethodTest();
            for (TestResult testResult : userPostResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userPatchResult;
            userPatchResult = resourceType.patchMethodTest();
            for (TestResult testResult : userPatchResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userSearchResult;
            userSearchResult = resourceType.searchMethodTest();
            for (TestResult testResult : userSearchResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userPutResult;
            userPutResult = resourceType.putMethodTest();
            for (TestResult testResult : userPutResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userDeleteResult;
            userDeleteResult = resourceType.deleteMethodTest();
            for (TestResult testResult : userDeleteResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userGetByIDResult;
            userGetByIDResult = resourceType.getByIdMethodTest();
            for (TestResult testResult : userGetByIDResult) {
                results.add(testResult);
            }

            // Group
            ArrayList<TestResult> groupGetResult;
            groupGetResult = resourceType2.getMethodTest();
            for (TestResult testResult : groupGetResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> groupPostResult;
            groupPostResult = resourceType2.postMethodTest();
            for (TestResult testResult : groupPostResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> groupPatchResult;
            groupPatchResult = resourceType2.patchMethodTest();
            for (TestResult testResult : groupPatchResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> groupSearchResult;
            groupSearchResult = resourceType2.searchMethodTest();
            for (TestResult testResult : groupSearchResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> groupPutResult;
            groupPutResult = resourceType2.putMethodTest();
            for (TestResult testResult : groupPutResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> groupDeleteResult;
            groupDeleteResult = resourceType2.deleteMethodTest();
            for (TestResult testResult : groupDeleteResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> groupGetByIDResult;
            groupGetByIDResult = resourceType2.getByIdMethodTest();
            for (TestResult testResult : groupGetByIDResult) {
                results.add(testResult);
            }

            // Me
            ArrayList<TestResult> meGetResult;
            meGetResult = resourceType6.getMethodTest();
            for (TestResult testResult : meGetResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> mePostResult;
            mePostResult = resourceType6.postMethodTest();
            for (TestResult testResult : mePostResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> mePatchResult;
            mePatchResult = resourceType6.patchMethodTest();
            for (TestResult testResult : mePatchResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> mePutResult;
            mePutResult = resourceType6.putMethodTest();
            for (TestResult testResult : mePutResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> meDeleteResult;
            meDeleteResult = resourceType6.deleteMethodTest();
            for (TestResult testResult : meDeleteResult) {
                results.add(testResult);
            }

            // Bulk
            ArrayList<TestResult> bulkPostResult;
            bulkPostResult = resourceType7.postMethodTest();
            for (TestResult testResult : bulkPostResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> bulkPatchResult;
            bulkPatchResult = resourceType7.patchMethodTest();
            for (TestResult testResult : bulkPatchResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> bulkPutResult;
            bulkPutResult = resourceType7.putMethodTest();
            for (TestResult testResult : bulkPutResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> bulkDeleteResult;
            bulkDeleteResult = resourceType7.deleteMethodTest();
            for (TestResult testResult : bulkDeleteResult) {
                results.add(testResult);
            }

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

            Result finalResults = new Result(statistics, results);

            //generate pdf results sheet
            try {
                String fullPath = "/home/anjanap/Desktop/SCIM2\n";
                String reportURL = PDFGenerator.generatePdfResults(finalResults, fullPath);
                //TODO : Change this on server
                finalResults.setReportLink("file://" + reportURL);
            } catch (IOException pdf) {

            }


            System.out.println("Success");
        } catch (Exception ee) {
            System.out.println("Error");
        }
    }
}
