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

package org.wso2.scim2.compliance.protocol;

import org.wso2.scim2.compliance.entities.Result;
import org.wso2.scim2.compliance.entities.Statistics;
import org.wso2.scim2.compliance.entities.TestResult;
import org.wso2.scim2.compliance.pdf.PDFGenerator;
import org.wso2.scim2.compliance.tests.ResourceType;

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

        EndpointFactory e = new EndpointFactory();
        ResourceType obj = e.getInstance("user");
        ResourceType obj2 = e.getInstance("group");
        ResourceType obj3 = e.getInstance("serviceProviderConfig");
        ResourceType obj4 = e.getInstance("resourceType");
        ResourceType obj5 = e.getInstance("schemaTest");
        ResourceType obj6 = e.getInstance("me");
        ResourceType obj7 = e.getInstance("bulk");
        ResourceType obj8 = e.getInstance("role");
        try {
            ArrayList<TestResult> results = new ArrayList<TestResult>();;
            ArrayList<TestResult> userGetResult;
            userGetResult = obj.getMethodTest();
            for (TestResult testResult : userGetResult) {
                results.add(testResult);
            }

            ArrayList<TestResult> userPostResult;
            userPostResult = obj.postMethodTest();
            for (TestResult testResult : userPostResult) {
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
            Result finalResults = new Result(statistics, results);

            //generate pdf results sheet
            try {
                String fullPath = "/home/anjanap/Desktop/SCIM2\n";
                String reportURL = PDFGenerator.generatePdfResults(finalResults, fullPath);
                //TODO : Change this on server
                finalResults.setReportLink("file://" + reportURL);
            } catch (IOException pdf) {
                System.out.println("Pdf error ");
            }

            System.out.println("Success");

        } catch (Exception ee) {
            System.out.println("error ");
            System.out.println(ee);
        }
    }
}
