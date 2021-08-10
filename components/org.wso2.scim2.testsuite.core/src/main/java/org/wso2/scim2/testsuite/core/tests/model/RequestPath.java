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

package org.wso2.scim2.testsuite.core.tests.model;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of  URL builder.
 */
public class RequestPath {

    private String url = StringUtils.EMPTY;
    private String testCaseName;
    private boolean supported = false;

    /**
     * Get url.
     *
     * @return url Target url to invoke tests.
     */
    public String getUrl() {

        return url;
    }

    /**
     * Set url.
     *
     * @param url Target url to invoke tests.
     */
    public void setUrl(String url) {

        this.url = url;
    }

    /**
     * Get testCaseName.
     *
     * @return testCaseName Current testcase name..
     */
    public String getTestCaseName() {

        return testCaseName;
    }

    /**
     * Set testCaseName.
     *
     * @param testCaseName Current testcase name.
     */
    public void setTestCaseName(String testCaseName) {

        this.testCaseName = testCaseName;
    }

    /**
     * Get supported value.
     *
     * @return supported Indicate whether testcase is supported by service provider.
     */
    public boolean getTestSupported() {

        return supported;
    }

    /**
     * Set supported value.
     *
     * @param supported Indicate whether testcase is supported by service provider.
     */
    public void setTestSupported(boolean supported) {

        this.supported = supported;
    }

}
