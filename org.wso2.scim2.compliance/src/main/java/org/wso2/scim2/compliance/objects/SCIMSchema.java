package org.wso2.scim2.compliance.objects;

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
            serviceProviderConfigSchema =  SCIMResourceSchemaManager.
                    getInstance().getServiceProviderConfigResourceSchema();
            return serviceProviderConfigSchema;
        }
        return serviceProviderConfigSchema;
    }

    public void setServiceProviderConfigSchema(SCIMResourceTypeSchema serviceProviderConfigSchema) {
        this.serviceProviderConfigSchema = serviceProviderConfigSchema;
    }

    public SCIMResourceTypeSchema getUserSchema() {
        if(userSchema == null){
            userSchema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
            return userSchema;
        }
        return userSchema;
    }

    public void setUserSchema(SCIMResourceTypeSchema userSchema) {
        this.userSchema = userSchema;
    }

    public SCIMResourceTypeSchema getGroupSchema() {
        if(groupSchema == null){
            groupSchema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
            return groupSchema;

        }
        return groupSchema;
    }

    public void setGroupSchema(SCIMResourceTypeSchema groupSchema) {
        this.groupSchema = groupSchema;
    }

    public SCIMResourceTypeSchema getResourceTypeSchema() {
        if (resourceTypeSchema == null){
            resourceTypeSchema = SCIMResourceSchemaManager.getInstance().getResourceTypeResourceSchema();
            return resourceTypeSchema;
        }
        return resourceTypeSchema;
    }

    public void setResourceTypeSchema(SCIMResourceTypeSchema resourceTypeSchema) {
        this.resourceTypeSchema = resourceTypeSchema;
    }
}
