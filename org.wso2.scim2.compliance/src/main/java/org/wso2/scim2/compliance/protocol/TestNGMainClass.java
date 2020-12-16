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

import org.testng.TestNG;
import org.wso2.scim2.compliance.tests.SchemaTestImpl;
import org.wso2.scim2.compliance.tests.UserTestImpl;

import java.util.ArrayList;
import java.util.List;

public class TestNGMainClass {

    public static void main(String[] args) {

        TestNG testSuite = new TestNG();
        testSuite.setTestClasses(new Class[]{SchemaTestImpl.class});
        ArrayList<String> l = new ArrayList<>();
        l.add("test2");
        testSuite.setTestNames(l);

        testSuite.addListener(new Test5SuiteListener());
        testSuite.setDefaultSuiteName("My Test Suite");
        testSuite.setDefaultTestName("My Test");
        // testSuite.setOutputDirectory("/home/anjanap/Desktop/hell");
      //  testSuite.run();
        //System.exit(testSuite.getStatus());


        TestNG testSuite2 = new TestNG();
        testSuite2.setTestClasses(new Class[]{UserTestImpl.class});


        testSuite2.addListener(new Test5SuiteListener());
        testSuite2.setDefaultSuiteName("My Test Suite2");
        testSuite2.setDefaultTestName("My Test2");
        // testSuite.setOutputDirectory("/home/anjanap/Desktop/hell");
        testSuite.run();
        System.exit(testSuite.getStatus());
    }
}
