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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.scim2.testsuite.core.entities.Result;
import org.wso2.scim2.testsuite.core.entities.Statistics;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.pdf.PDFGenerator;
import org.wso2.scim2.testsuite.core.tests.ResourceType;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class invoke all tests in compliance test suite.
 */
public class Compliance {

    private static final Log logger = LogFactory.getLog(Compliance.class);

    public static void main(String[] arg) {

        EndpointFactory endFactory = new EndpointFactory("https://localhost:9443/scim2", "admin", "admin", "");
        ResourceType user = endFactory.getInstance(ComplianceConstants.EndPointConstants.USER);
        ResourceType group = endFactory.getInstance(ComplianceConstants.EndPointConstants.GROUP);
        ResourceType serviceProviderConfig =
                endFactory.getInstance(ComplianceConstants.EndPointConstants.SERVICEPROVIDERCONFIG);
        ResourceType resourceType = endFactory.getInstance(ComplianceConstants.EndPointConstants.RESOURCETYPE);
        ResourceType schema = endFactory.getInstance(ComplianceConstants.EndPointConstants.SCHEMAS);
        ResourceType self = endFactory.getInstance(ComplianceConstants.EndPointConstants.ME);
        ResourceType bulk = endFactory.getInstance(ComplianceConstants.EndPointConstants.BULK);

        try {
            // Invoke ServiceProviderConfig test.
            ArrayList<TestResult> serviceProviderResult;
            serviceProviderResult = serviceProviderConfig.getMethodTest();
            ArrayList<TestResult> results = new ArrayList<>(serviceProviderResult);

            // Invoke ResourceTypes test.
            ArrayList<TestResult> resourceTypeResult;
            resourceTypeResult = resourceType.getMethodTest();
            results.addAll(resourceTypeResult);

            // Invoke schemas test.
            ArrayList<TestResult> schemaTestResult;
            schemaTestResult = schema.getMethodTest();
            results.addAll(schemaTestResult);

            // Invoke user related tests.
            ArrayList<TestResult> userGetResult;
            userGetResult = user.getMethodTest();
            results.addAll(userGetResult);

            ArrayList<TestResult> userPostResult;
            userPostResult = user.postMethodTest();
            results.addAll(userPostResult);

            ArrayList<TestResult> userPatchResult;
            userPatchResult = user.patchMethodTest();
            results.addAll(userPatchResult);

            ArrayList<TestResult> userSearchResult;
            userSearchResult = user.searchMethodTest();
            results.addAll(userSearchResult);

            ArrayList<TestResult> userPutResult;
            userPutResult = user.putMethodTest();
            results.addAll(userPutResult);

            ArrayList<TestResult> userDeleteResult;
            userDeleteResult = user.deleteMethodTest();
            results.addAll(userDeleteResult);

            ArrayList<TestResult> userGetByIDResult;
            userGetByIDResult = user.getByIdMethodTest();
            results.addAll(userGetByIDResult);

            // Invoke group related tests.
            ArrayList<TestResult> groupGetResult;
            groupGetResult = group.getMethodTest();
            results.addAll(groupGetResult);

            ArrayList<TestResult> groupPostResult;
            groupPostResult = group.postMethodTest();
            results.addAll(groupPostResult);

            ArrayList<TestResult> groupPatchResult;
            groupPatchResult = group.patchMethodTest();
            results.addAll(groupPatchResult);

            ArrayList<TestResult> groupSearchResult;
            groupSearchResult = group.searchMethodTest();
            results.addAll(groupSearchResult);

            ArrayList<TestResult> groupPutResult;
            groupPutResult = group.putMethodTest();
            results.addAll(groupPutResult);

            ArrayList<TestResult> groupDeleteResult;
            groupDeleteResult = group.deleteMethodTest();
            results.addAll(groupDeleteResult);

            ArrayList<TestResult> groupGetByIDResult;
            groupGetByIDResult = group.getByIdMethodTest();
            results.addAll(groupGetByIDResult);

            // Invoke Me related tests.
            ArrayList<TestResult> meGetResult;
            meGetResult = self.getMethodTest();
            results.addAll(meGetResult);

            ArrayList<TestResult> mePostResult;
            mePostResult = self.postMethodTest();
            results.addAll(mePostResult);

            ArrayList<TestResult> mePatchResult;
            mePatchResult = self.patchMethodTest();
            results.addAll(mePatchResult);

            ArrayList<TestResult> mePutResult;
            mePutResult = self.putMethodTest();
            results.addAll(mePutResult);

            ArrayList<TestResult> meDeleteResult;
            meDeleteResult = self.deleteMethodTest();
            results.addAll(meDeleteResult);

            // Invoke Bulk related tests.
            ArrayList<TestResult> bulkPostResult;
            bulkPostResult = bulk.postMethodTest();
            results.addAll(bulkPostResult);

            ArrayList<TestResult> bulkPatchResult;
            bulkPatchResult = bulk.patchMethodTest();
            results.addAll(bulkPatchResult);

            ArrayList<TestResult> bulkPutResult;
            bulkPutResult = bulk.putMethodTest();
            results.addAll(bulkPutResult);

            ArrayList<TestResult> bulkDeleteResult;
            bulkDeleteResult = bulk.deleteMethodTest();
            results.addAll(bulkDeleteResult);

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

            Result finalResults = new Result(statistics, results);
            // Get absolute path of root directory.
            String pathTemp = System.getProperty("user.dir");
            // Generate pdf results sheet.
            try {
                String fullPath = pathTemp + "/components/org.wso2.scim2.testsuite.core/target/SCIM 2.0 Compliance " +
                        "Test Suite - " +
                        "Auto Generated Test Report";
                String reportURL = PDFGenerator.generatePdfResults(finalResults, fullPath);
                finalResults.setReportLink("file://" + reportURL);
            } catch (IOException pdfError) {
                logger.error("PDF generation failed with error : ", pdfError);
            }
        } catch (Exception e) {
            logger.error("Test execution failed with error : ", e);
        }
    }
}
