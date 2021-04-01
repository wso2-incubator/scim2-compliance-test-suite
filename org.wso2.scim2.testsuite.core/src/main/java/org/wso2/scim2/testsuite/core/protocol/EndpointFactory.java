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

/**
 * Method for calling factory.
 */
public class EndpointFactory {

    private String url;
    private String userName;
    private String password;
    private String token;

    /**
     * Initialize.
     *
     * @param url,userName,password,token
     */
    public EndpointFactory(String url, String userName, String password, String token) {

        this.url = url;
        this.userName = userName;
        this.password = password;
        this.token = token;
    }

    public ResourceType getInstance(String str) {

        ComplianceTestMetaDataHolder complianceTestMetaDataHolder = new ComplianceTestMetaDataHolder();
        complianceTestMetaDataHolder.setUrl(url);
        complianceTestMetaDataHolder.setUsername(userName);
        complianceTestMetaDataHolder.setPassword(password);
//        complianceTestMetaDataHolder.setAuthorization_server(authorizationServer);
        complianceTestMetaDataHolder.setAuthorization_header(token);
//        complianceTestMetaDataHolder.setAuthorization_method(authMethod);
//        complianceTestMetaDataHolder.setClient_id(clientId);
//        complianceTestMetaDataHolder.setClient_secret(clientSecret);

        SCIMSchema scimSchema = new SCIMSchema();

        // set the scim schema object
        complianceTestMetaDataHolder.setScimSchema(scimSchema);

        if (str.equals("serviceProviderConfig")) {
            return new ServiceProviderConfigTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("user")) {
            return new UserTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("group")) {
            return new GroupTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("resourceType")) {
            return new ResourceTypeTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("schemaTest")) {
            return new SchemaTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("me")) {
            return new MeTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("bulk")) {
            return new BulkTestImpl(complianceTestMetaDataHolder);
        } else if (str.equals("role")) {
            return new RolesTestImpl(complianceTestMetaDataHolder);
        }
        return null;
    }
}
