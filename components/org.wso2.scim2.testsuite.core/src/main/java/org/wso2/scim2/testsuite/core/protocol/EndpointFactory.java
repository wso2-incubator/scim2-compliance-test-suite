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

import org.wso2.scim2.testsuite.core.objects.SCIMSchema;
import org.wso2.scim2.testsuite.core.tests.BulkTestImpl;
import org.wso2.scim2.testsuite.core.tests.GroupTestImpl;
import org.wso2.scim2.testsuite.core.tests.MeTestImpl;
import org.wso2.scim2.testsuite.core.tests.ResourceType;
import org.wso2.scim2.testsuite.core.tests.ResourceTypeTestImpl;
import org.wso2.scim2.testsuite.core.tests.RolesTestImpl;
import org.wso2.scim2.testsuite.core.tests.SchemaTestImpl;
import org.wso2.scim2.testsuite.core.tests.ServiceProviderConfigTestImpl;
import org.wso2.scim2.testsuite.core.tests.UserTestImpl;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

/**
 * This class contains test endpoints for the test suite.
 */
public class EndpointFactory {

    private final String url;
    private final String userName;
    private final String password;
    private final String token;

    /**
     * @param url      Service Provider endpoint.
     * @param userName For basic authentication.
     * @param password For basic authentication.
     * @param token    For bearer token based authentication.
     */
    public EndpointFactory(String url, String userName, String password, String token) {

        this.url = url;
        this.userName = userName;
        this.password = password;
        this.token = token;
    }

    /**
     * Method provide relevant test endpoint.
     *
     * @param endpoint Indicate which test endpoint to run.
     * @return ResourceType Return corresponding resource object.
     */
    public ResourceType getInstance(String endpoint) {

        ComplianceTestMetaDataHolder complianceTestMetaDataHolder = new ComplianceTestMetaDataHolder();
        complianceTestMetaDataHolder.setUrl(url);
        complianceTestMetaDataHolder.setUsername(userName);
        complianceTestMetaDataHolder.setPassword(password);
        complianceTestMetaDataHolder.setAuthorization_header(token);

        SCIMSchema scimSchema = new SCIMSchema();

        // Set the scim schema object.
        complianceTestMetaDataHolder.setScimSchema(scimSchema);

        switch (endpoint) {
            case ComplianceConstants.EndPointConstants.SERVICEPROVIDERCONFIG:
                return new ServiceProviderConfigTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.USER:
                return new UserTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.GROUP:
                return new GroupTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.RESOURCETYPE:
                return new ResourceTypeTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.SCHEMAS:
                return new SchemaTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.ME:
                return new MeTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.BULK:
                return new BulkTestImpl(complianceTestMetaDataHolder);
            case ComplianceConstants.EndPointConstants.ROLE:
                return new RolesTestImpl(complianceTestMetaDataHolder);
        }
        return null;
    }
}
