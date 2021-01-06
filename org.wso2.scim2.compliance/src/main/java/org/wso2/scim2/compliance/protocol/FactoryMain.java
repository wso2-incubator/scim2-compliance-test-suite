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

import org.wso2.scim2.compliance.entities.TestResult;
import org.wso2.scim2.compliance.tests.ResourceType;

import java.util.ArrayList;

/**
 * Method for calling factory.
 */
public class FactoryMain {

    public static void main(String a[]) {

        EndpointFactory e = new EndpointFactory();
        ResourceType obj = e.getInstance("user");
        ResourceType obj2 = e.getInstance("group");
        ResourceType obj3 = e.getInstance("serviceProviderConfig");
        try {
            ArrayList<TestResult> userTestResults;
           //userTestResults = obj.getMethodTest();

            //obj2.getMethodTest();
           obj2.getMethodTest();
            //obj.patchMethodTest();
            System.out.println("Success");

        } catch (Exception ee) {
            System.out.println("error ");
            System.out.println(ee);
        }
    }
}
