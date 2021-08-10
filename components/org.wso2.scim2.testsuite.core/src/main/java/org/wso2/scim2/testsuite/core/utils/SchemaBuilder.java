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

package org.wso2.scim2.testsuite.core.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.charon3.core.schema.AttributeSchema;
import org.wso2.charon3.core.schema.SCIMAttributeSchema;
import org.wso2.charon3.core.schema.SCIMConstants;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.CriticalComplianceException;
import org.wso2.scim2.testsuite.core.objects.SCIMSchema;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class build schema representations using the /Schema Endpoint.
 */
public class SchemaBuilder {

    /**
     * This create the schemas according to service provider as defined by /Schemas endpoint.
     *
     * @param jsonSchema     Json response by service provider.
     * @param method         Http request type.
     * @param headerString   Response headers from service provider.
     * @param responseStatus Status code of response.
     * @param subTests       Assertions done for each test case.
     * @param url            Url of request.
     * @param scimSchema     Resource schema.
     * @throws ComplianceException         Constructed new exception with the specified detail message.
     * @throws CriticalComplianceException Critical exception.
     */
    public static void buildSchema(String jsonSchema,
                                   HttpGet method,
                                   String headerString,
                                   String responseStatus,
                                   ArrayList<String> subTests,
                                   String url,
                                   SCIMSchema scimSchema)
            throws ComplianceException, CriticalComplianceException {

        try {
            JSONArray jsonArray = new JSONArray(jsonSchema);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject resourceObject = jsonArray.getJSONObject(i);
                String resourceId;
                try {
                    resourceId = resourceObject.optString("id");
                } catch (NullPointerException e) {
                    throw new CriticalComplianceException(new TestResult
                            (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                    "Could not get schema at url " + url,
                                    ComplianceUtils.getWire(method, jsonSchema,
                                            headerString, responseStatus, subTests)));
                }
                switch (resourceId) {
                    case "urn:ietf:params:scim:schemas:core:2.0:User":
                        // Set the user schema.
                        scimSchema.setUserSchema(buildResourceSchema(resourceId, resourceObject,
                                responseStatus, method, headerString, url, jsonSchema, subTests,
                                new ArrayList<>(Collections.singletonList(SCIMConstants.USER_CORE_SCHEMA_URI))));
                        break;
                    case "urn:ietf:params:scim:schemas:core:2.0:Group":
                        // Set the group schema.
                        scimSchema.setGroupSchema(buildResourceSchema(resourceId, resourceObject,
                                responseStatus, method, headerString, url, jsonSchema, subTests,
                                new ArrayList<>(Collections.singletonList(SCIMConstants.GROUP_CORE_SCHEMA_URI))));
                        break;
                    case "urn:ietf:params:scim:schemas:core:2.0:ResourceType":
                        // Set the resource type schema.
                        scimSchema.setResourceTypeSchema(buildResourceSchema(resourceId, resourceObject,
                                responseStatus, method, headerString, url, jsonSchema, subTests,
                                new ArrayList<>(Collections.singletonList(SCIMConstants.RESOURCE_TYPE_SCHEMA_URI))));
                        break;
                    case "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig":
                        // Set the user service provider schema.
                        scimSchema.setServiceProviderConfigSchema(buildResourceSchema(resourceId, resourceObject,
                                responseStatus, method, headerString, url, jsonSchema, subTests,
                                new ArrayList<>(Collections.singletonList(SCIMConstants.
                                        SERVICE_PROVIDER_CONFIG_SCHEMA_URI))));
                        break;
                    case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":
                        // Set the user extension schema.
                        ArrayList currentAttributes = scimSchema.getUserSchema().getAttributesList();
                        SCIMResourceTypeSchema extensionSchema = buildResourceSchema(resourceId, resourceObject,
                                responseStatus, method, headerString, url, jsonSchema, subTests,
                                new ArrayList<>(Collections.singletonList(ComplianceConstants.TestConstants.
                                        EXTENSION_SCHEMA_URI)));
                        currentAttributes.add(extensionSchema.getAttributesList());
                        scimSchema.getUserSchema().setAttributeList(currentAttributes);
                        break;
                }
            }
        } catch (JSONException e) {
            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                            "Could not get schema at url " + url,
                            ComplianceUtils.getWire(method, jsonSchema,
                                    headerString, responseStatus, subTests)));
        }
    }

    /**
     * This build the charon defined resource schema.
     *
     * @param resourceId     Resource id.
     * @param resourceObject Resource object.
     * @param responseStatus Status code of response.
     * @param method         Http request type.
     * @param headerString   Response headers from service provider.
     * @param url            Url of request.
     * @param jsonSchema     Json schema.
     * @param subTests       Assertions done for each test case.
     * @param schemaURIs     List of schema uris.
     * @return SCIMResourceTypeSchema Return created scim resource schema.
     * @throws ComplianceException         Constructed new exception with the specified detail message.
     * @throws CriticalComplianceException Critical exception.
     */
    public static SCIMResourceTypeSchema buildResourceSchema(String resourceId,
                                                             JSONObject resourceObject,
                                                             String responseStatus,
                                                             HttpGet method,
                                                             String headerString,
                                                             String url,
                                                             String jsonSchema,
                                                             ArrayList<String> subTests,
                                                             ArrayList<String> schemaURIs)
            throws ComplianceException, CriticalComplianceException {

        ArrayList<SCIMAttributeSchema> attributeSchemaList = new ArrayList<>();
        JSONArray attributeList;
        try {
            attributeList = resourceObject.optJSONArray("attributes");
        } catch (NullPointerException e) {
            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                            "Could not get schema at url " + url,
                            ComplianceUtils.getWire(method, jsonSchema,
                                    headerString, responseStatus, subTests)));
        }

        for (int j = 0; j < attributeList.length(); j++) {
            JSONObject attribute;
            try {
                attribute = attributeList.getJSONObject(j);
            } catch (JSONException e) {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String name;
            if (attribute.optString("name") != null) {
                name = attribute.optString("name");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            SCIMAttributeSchema scimAttributeSchema = buildAttributeSchema
                    (attribute, resourceId + ":" + name,
                            method, responseStatus, subTests, headerString, jsonSchema, url);

            ArrayList<AttributeSchema> subAttributes = new ArrayList<>();

            if (attribute.has("subAttributes")) {
                JSONArray subAttributesArray = attribute.optJSONArray("subAttributes");
                for (int k = 0; k < subAttributesArray.length(); k++) {
                    JSONObject subAttribute;
                    try {
                        subAttribute = subAttributesArray.getJSONObject(k);
                    } catch (JSONException e) {
                        throw new CriticalComplianceException(new TestResult
                                (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                        "Could not get schema at url " + url,
                                        ComplianceUtils.getWire(method, jsonSchema,
                                                headerString, responseStatus, subTests)));
                    }
                    String subName;
                    if (subAttribute.optString("name") != null) {
                        subName = subAttribute.optString("name");
                    } else {
                        throw new CriticalComplianceException(new TestResult
                                (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                        "Could not get schema at url " + url,
                                        ComplianceUtils.getWire(method, jsonSchema,
                                                headerString, responseStatus, subTests)));
                    }
                    SCIMAttributeSchema scimSubAttributeSchema =
                            buildAttributeSchema(subAttribute, scimAttributeSchema.getURI()
                                            + "." + subName, method, responseStatus,
                                    subTests, headerString, jsonSchema, url);
                    subAttributes.add(scimSubAttributeSchema);

                }
            }
            scimAttributeSchema.setSubAttributes(subAttributes);
            attributeSchemaList.add(scimAttributeSchema);
        }
        return SCIMResourceTypeSchema.createSCIMResourceSchema(schemaURIs,
                attributeSchemaList.toArray(new SCIMAttributeSchema[0]));
    }

    /**
     * This method build the charon defined attribute schema.
     *
     * @param attribute      Attribute of schema.
     * @param uri            Uri of request.
     * @param method         Http request type.
     * @param responseStatus Status code of response.
     * @param subTests       Assertions done for each test case.
     * @param headerString   Response headers from service provider.
     * @param jsonSchema     Json schema.
     * @param url            url of request.
     * @return SCIMAttributeSchema Return created scim attribute schema
     * @throws ComplianceException         Constructed new exception with the specified detail message.
     * @throws CriticalComplianceException Critical exception.
     */
    public static SCIMAttributeSchema buildAttributeSchema(JSONObject attribute,
                                                           String uri,
                                                           HttpGet method,
                                                           String responseStatus,
                                                           ArrayList<String> subTests,
                                                           String headerString,
                                                           String jsonSchema,
                                                           String url)
            throws ComplianceException, CriticalComplianceException {

        String attributeName = null;
        try {
            String name;
            if (attribute.optString("name") != null) {
                name = attribute.optString("name");
                attributeName = name;
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String type;
            if (attribute.optString("type") != null) {
                type = attribute.optString("type");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            boolean multivalued;
            if (attribute.get("multiValued") != null) {
                multivalued = attribute.optBoolean("multiValued");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String description;
            if (attribute.optString("description") != null) {
                description = attribute.optString("description");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            boolean required;
            if (attribute.get("required") != null) {
                required = attribute.optBoolean("required");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            boolean caseExact;
            if (attribute.get("caseExact") != null) {
                caseExact = attribute.optBoolean("caseExact");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String mutability;
            if (attribute.optString("mutability") != null) {
                mutability = attribute.optString("mutability");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String returned;
            if (attribute.optString("returned") != null) {
                returned = attribute.optString("returned");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String uniqueness;
            if (attribute.optString("uniqueness") != null) {
                uniqueness = attribute.optString("uniqueness");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }

            SCIMDefinitions.Mutability mutabilityDefinition = null;
            SCIMDefinitions.DataType dataTypeDefinition = null;
            SCIMDefinitions.Returned returnedDefinition = null;
            SCIMDefinitions.Uniqueness uniquenessDefinition = null;

            switch (mutability) {
                case "readWrite":
                    mutabilityDefinition = SCIMDefinitions.Mutability.READ_WRITE;
                    break;
                case "readOnly":
                    mutabilityDefinition = SCIMDefinitions.Mutability.READ_ONLY;
                    break;
                case "writeOnly":
                    mutabilityDefinition = SCIMDefinitions.Mutability.WRITE_ONLY;
                    break;
                case "immutable":
                    mutabilityDefinition = SCIMDefinitions.Mutability.IMMUTABLE;
                    break;
            }

            switch (type) {
                case "binary":
                    dataTypeDefinition = SCIMDefinitions.DataType.BINARY;
                    break;
                case "boolean":
                    dataTypeDefinition = SCIMDefinitions.DataType.BOOLEAN;
                    break;
                case "complex":
                    dataTypeDefinition = SCIMDefinitions.DataType.COMPLEX;
                    break;
                case "dataTime":
                    dataTypeDefinition = SCIMDefinitions.DataType.DATE_TIME;
                    break;
                case "decimal":
                    dataTypeDefinition = SCIMDefinitions.DataType.DECIMAL;
                    break;
                case "integer":
                    dataTypeDefinition = SCIMDefinitions.DataType.INTEGER;
                    break;
                case "reference":
                    dataTypeDefinition = SCIMDefinitions.DataType.REFERENCE;
                    break;
                case "string":
                    dataTypeDefinition = SCIMDefinitions.DataType.STRING;
                    break;
            }

            switch (returned) {
                case "always":
                    returnedDefinition = SCIMDefinitions.Returned.ALWAYS;
                    break;
                case "default":
                    returnedDefinition = SCIMDefinitions.Returned.DEFAULT;
                    break;
                case "never":
                    returnedDefinition = SCIMDefinitions.Returned.NEVER;
                    break;
                case "request":
                    returnedDefinition = SCIMDefinitions.Returned.REQUEST;
                    break;
            }

            switch (uniqueness) {
                case "global":
                    uniquenessDefinition = SCIMDefinitions.Uniqueness.GLOBAL;
                    break;
                case "none":
                    uniquenessDefinition = SCIMDefinitions.Uniqueness.NONE;
                    break;
                case "server":
                    uniquenessDefinition = SCIMDefinitions.Uniqueness.SERVER;
                    break;
            }

            subTests.add("Validate the attribute definitions of " + attributeName);
            subTests.add("Test description : Check attribute definition follow SCIM specification.");
            subTests.add(ComplianceConstants.TestConstants.STATUS_SUCCESS);
            subTests.add(StringUtils.EMPTY);

            return SCIMAttributeSchema.createSCIMAttributeSchema(uri, name, dataTypeDefinition,
                    multivalued, description, required, caseExact, mutabilityDefinition,
                    returnedDefinition, uniquenessDefinition,
                    null, null, null);

        } catch (JSONException e) {
            subTests.add("Validate the attribute definitions of " + attributeName);
            subTests.add("Test description : Check attribute definition follow SCIM specification.");
            subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
            subTests.add(StringUtils.EMPTY);
            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, ComplianceConstants.TestConstants.GET_SCHEMAS,
                            e.getMessage() + "in attribute " + attributeName,
                            ComplianceUtils.getWire(method, jsonSchema,
                                    headerString, responseStatus, subTests)));
        }
    }
}
