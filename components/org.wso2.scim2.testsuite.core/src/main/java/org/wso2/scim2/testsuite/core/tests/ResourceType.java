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

package org.wso2.scim2.testsuite.core.tests;

import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;

import java.util.ArrayList;

/**
 * Interface for scim test cases.
 */
public interface ResourceType {

    ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> searchMethodTest() throws GeneralComplianceException, ComplianceException;

    ArrayList<TestResult> executeAllTests() throws GeneralComplianceException, ComplianceException;

}
