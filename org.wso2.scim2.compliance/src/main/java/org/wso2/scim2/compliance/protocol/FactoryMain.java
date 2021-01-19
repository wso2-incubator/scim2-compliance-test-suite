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
        ResourceType obj4 = e.getInstance("resourceType");
        ResourceType obj5 = e.getInstance("schemaTest");
        ResourceType obj6 = e.getInstance("me");
        ResourceType obj7 = e.getInstance("bulk");
        try {
            ArrayList<TestResult> userTestResults;
            //  obj3.getMethodTest();
            //  obj.getMethodTest();
            // obj.patchMethodTest();
            // obj2.patchMethodTest();
            //obj.patchMethodTest();
            obj7.deleteMethodTest();
            //  obj4.getMethodTest();
            //obj5.getMethodTest();
            //userTestResults = obj.getByIdMethodTest();
//            obj2.patchMethodTest();
//            obj2.getByIdMethodTest();
//            obj2.searchMethodTest();
//            obj2.deleteMethodTest();
//
//            obj2.getMethodTest();
//            obj2.postMethodTest();
            //  obj3.getMethodTest();
            //obj.patchMethodTest();
//            obj2.putMethodTest();
            // obj6.patchMethodTest();
            //obj.patchMethodTest();
            System.out.println("Success");

        } catch (Exception ee) {
            System.out.println("error ");
            System.out.println(ee);
        }
    }
}
