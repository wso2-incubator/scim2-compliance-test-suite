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

import org.wso2.scim2.compliance.objects.SCIMSchema;
import org.wso2.scim2.compliance.tests.GroupTestImpl;
import org.wso2.scim2.compliance.tests.ResourceType;
import org.wso2.scim2.compliance.tests.ServiceProviderConfigTestImpl;
import org.wso2.scim2.compliance.tests.UserTestImpl;

/**
 * Method for calling factory.
 */
public class EndpointFactory {

    public ResourceType getInstance(String str) {

        ComplianceTestMetaDataHolder complianceTestMetaDataHolder = new ComplianceTestMetaDataHolder();
        complianceTestMetaDataHolder.setUrl("https://localhost:9443/scim2");
        complianceTestMetaDataHolder.setUsername("admin");
        complianceTestMetaDataHolder.setPassword("admin");
//        complianceTestMetaDataHolder.setAuthorization_server(authorizationServer);
//        complianceTestMetaDataHolder.setAuthorization_header(authorizationHeader);
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
        }
        return null;
    }
}
