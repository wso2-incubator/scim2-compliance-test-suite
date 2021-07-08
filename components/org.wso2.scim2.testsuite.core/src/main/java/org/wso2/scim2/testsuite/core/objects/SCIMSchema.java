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

package org.wso2.scim2.testsuite.core.objects;

import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

/**
 * This class holds the schemas according to service provider.
 */
public class SCIMSchema {

    private SCIMResourceTypeSchema serviceProviderConfigSchema = null;
    private SCIMResourceTypeSchema userSchema = null;
    private SCIMResourceTypeSchema groupSchema = null;
    private SCIMResourceTypeSchema resourceTypeSchema = null;

    public SCIMResourceTypeSchema getServiceProviderConfigSchema() {

        if (serviceProviderConfigSchema == null) {
            serviceProviderConfigSchema = SCIMResourceSchemaManager.
                    getInstance().getServiceProviderConfigResourceSchema();
            return serviceProviderConfigSchema;
        }
        return serviceProviderConfigSchema;
    }

    public void setServiceProviderConfigSchema(SCIMResourceTypeSchema serviceProviderConfigSchema) {

        this.serviceProviderConfigSchema = serviceProviderConfigSchema;
    }

    public SCIMResourceTypeSchema getUserSchema() {

        if (userSchema == null) {
            userSchema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
            return userSchema;
        }
        return userSchema;
    }

    public void setUserSchema(SCIMResourceTypeSchema userSchema) {

        this.userSchema = userSchema;
    }

    public SCIMResourceTypeSchema getGroupSchema() {

        if (groupSchema == null) {
            groupSchema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
            return groupSchema;

        }
        return groupSchema;
    }

    public void setGroupSchema(SCIMResourceTypeSchema groupSchema) {

        this.groupSchema = groupSchema;
    }

    public SCIMResourceTypeSchema getResourceTypeSchema() {

        if (resourceTypeSchema == null) {
            resourceTypeSchema = SCIMResourceSchemaManager.getInstance().getResourceTypeResourceSchema();
            return resourceTypeSchema;
        }
        return resourceTypeSchema;
    }

    public void setResourceTypeSchema(SCIMResourceTypeSchema resourceTypeSchema) {

        this.resourceTypeSchema = resourceTypeSchema;
    }
}
