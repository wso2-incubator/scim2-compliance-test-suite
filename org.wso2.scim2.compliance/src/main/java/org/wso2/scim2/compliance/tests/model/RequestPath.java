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

package org.wso2.scim2.compliance.tests.model;

/**
 * Implementation of  URL builder.
 */
public class RequestPath {

    private String url;
    private String testCaseName;
    private boolean supported = true;

    /**
     * Get url.
     *
     * @return url
     */
    public String getUrl() {

        return url;
    }

    /**
     * Set url.
     *
     * @param url to Function setUrl
     */
    public void setUrl(String url) {

        this.url = url;
    }

    /**
     * Get testCaseName.
     *
     * @return testCaseName
     */
    public String getTestCaseName() {

        return testCaseName;
    }

    /**
     * Set testCaseName.
     *
     * @param testCaseName to Function set testCaseName
     */
    public void setTestCaseName(String testCaseName) {

        this.testCaseName = testCaseName;
    }

    /**
     * Get supported value.
     *
     * @return supported value.
     */
    public boolean getTestSupported() {

        return supported;
    }

    /**
     * Set supported value..
     *
     * @param supported value to Function setTestSupported
     */
    public void setTestSupported(boolean supported) {

        this.supported = supported;
    }

}
