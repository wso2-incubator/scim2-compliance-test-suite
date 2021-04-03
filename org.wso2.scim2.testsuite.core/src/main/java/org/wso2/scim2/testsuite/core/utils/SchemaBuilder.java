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
import java.util.Arrays;

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
                String resourceId = null;
                try {
                    resourceId = resourceObject.optString("id");
                } catch (NullPointerException e) {
                    throw new CriticalComplianceException(new TestResult
                            (TestResult.ERROR, "Get Schema",
                                    "Could not get schema at url " + url,
                                    ComplianceUtils.getWire(method, jsonSchema,
                                            headerString, responseStatus, subTests)));
                }
                if (resourceId.equals("urn:ietf:params:scim:schemas:core:2.0:User")) {
                    // Set the user schema.
                    scimSchema.setUserSchema(buildResourceSchema(resourceId, resourceObject,
                            responseStatus, method, headerString, url, jsonSchema, subTests,
                            new ArrayList<String>(Arrays.asList(SCIMConstants.USER_CORE_SCHEMA_URI))));
                } else if (resourceId.equals("urn:ietf:params:scim:schemas:core:2.0:Group")) {
                    // Set the group schema.
                    scimSchema.setGroupSchema(buildResourceSchema(resourceId, resourceObject,
                            responseStatus, method, headerString, url, jsonSchema, subTests,
                            new ArrayList<String>(Arrays.asList(SCIMConstants.GROUP_CORE_SCHEMA_URI))));
                } else if (resourceId.equals("urn:ietf:params:scim:schemas:core:2.0:ResourceType")) {
                    // Set the resource type schema.
                    scimSchema.setResourceTypeSchema(buildResourceSchema(resourceId, resourceObject,
                            responseStatus, method, headerString, url, jsonSchema, subTests,
                            new ArrayList<String>(Arrays.asList(SCIMConstants.RESOURCE_TYPE_SCHEMA_URI))));
                } else if (resourceId.equals("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig")) {
                    // Set the user service provider schema.
                    scimSchema.setServiceProviderConfigSchema(buildResourceSchema(resourceId, resourceObject,
                            responseStatus, method, headerString, url, jsonSchema, subTests,
                            new ArrayList<String>(Arrays.asList(SCIMConstants.SERVICE_PROVIDER_CONFIG_SCHEMA_URI))));
                } else if (resourceId.equals("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User")) {
                    // Set the user extension schema.
                    ArrayList currentAttributes = scimSchema.getUserSchema().getAttributesList();
                    SCIMResourceTypeSchema extensionSchema = buildResourceSchema(resourceId, resourceObject,
                            responseStatus, method, headerString, url, jsonSchema, subTests,
                            new ArrayList<String>(Arrays.asList(ComplianceConstants.TestConstants.
                                    EXTENSION_SCHEMA_URI)));
                    currentAttributes.add(extensionSchema.getAttributesList());
                    scimSchema.getUserSchema().setAttributeList(currentAttributes);
                }
            }
        } catch (JSONException e) {
            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, "Get Schema",
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
        JSONArray attributeList = null;
        try {
            attributeList = resourceObject.optJSONArray("attributes");
        } catch (NullPointerException e) {
            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, "Get Schema",
                            "Could not get schema at url " + url,
                            ComplianceUtils.getWire(method, jsonSchema,
                                    headerString, responseStatus, subTests)));
        }

        for (int j = 0; j < attributeList.length(); j++) {
            JSONObject attribute = null;
            try {
                attribute = attributeList.getJSONObject(j);
            } catch (JSONException e) {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String name = "";
            if (attribute.optString("name") != null) {
                name = attribute.optString("name");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
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
                    JSONObject subAttribute = null;
                    try {
                        subAttribute = subAttributesArray.getJSONObject(k);
                    } catch (JSONException e) {
                        throw new CriticalComplianceException(new TestResult
                                (TestResult.ERROR, "Get Schema",
                                        "Could not get schema at url " + url,
                                        ComplianceUtils.getWire(method, jsonSchema,
                                                headerString, responseStatus, subTests)));
                    }
                    String subName = "";
                    if (subAttribute.optString("name") != null) {
                        subName = subAttribute.optString("name");
                    } else {
                        throw new CriticalComplianceException(new TestResult
                                (TestResult.ERROR, "Get Schema",
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
                attributeSchemaList.toArray(new SCIMAttributeSchema[attributeSchemaList.size()]));
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
            String name = "";
            if (attribute.optString("name") != null) {
                name = attribute.optString("name");
                attributeName = name;
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String type = "";
            if (attribute.optString("type") != null) {
                type = attribute.optString("type");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            boolean multivalued = false;
            if (attribute.get("multiValued") != null) {
                multivalued = attribute.optBoolean("multiValued");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String description = "";
            if (attribute.optString("description") != null) {
                description = attribute.optString("description");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            boolean required = false;
            if (attribute.get("required") != null) {
                required = attribute.optBoolean("required");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            boolean caseExact = false;
            if (attribute.get("caseExact") != null) {
                caseExact = attribute.optBoolean("caseExact");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String mutability = "";
            if (attribute.optString("mutability") != null) {
                mutability = attribute.optString("mutability");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String returned = "";
            if (attribute.optString("returned") != null) {
                returned = attribute.optString("returned");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }
            String uniqueness = "";
            if (attribute.optString("uniqueness") != null) {
                uniqueness = attribute.optString("uniqueness");
            } else {
                throw new CriticalComplianceException(new TestResult
                        (TestResult.ERROR, "Get Schema",
                                "Could not get schema at url " + url,
                                ComplianceUtils.getWire(method, jsonSchema,
                                        headerString, responseStatus, subTests)));
            }

            SCIMDefinitions.Mutability mutabilityDefinition = null;
            SCIMDefinitions.DataType dataTypeDefinition = null;
            SCIMDefinitions.Returned returnedDefinition = null;
            SCIMDefinitions.Uniqueness uniquenessDefinition = null;

            if (mutability.equals("readWrite")) {
                mutabilityDefinition = SCIMDefinitions.Mutability.READ_WRITE;
            } else if (mutability.equals("readOnly")) {
                mutabilityDefinition = SCIMDefinitions.Mutability.READ_ONLY;
            } else if (mutability.equals("writeOnly")) {
                mutabilityDefinition = SCIMDefinitions.Mutability.WRITE_ONLY;
            } else if (mutability.equals("immutable")) {
                mutabilityDefinition = SCIMDefinitions.Mutability.IMMUTABLE;
            }

            if (type.equals("binary")) {
                dataTypeDefinition = SCIMDefinitions.DataType.BINARY;
            } else if (type.equals("boolean")) {
                dataTypeDefinition = SCIMDefinitions.DataType.BOOLEAN;
            } else if (type.equals("complex")) {
                dataTypeDefinition = SCIMDefinitions.DataType.COMPLEX;
            } else if (type.equals("dataTime")) {
                dataTypeDefinition = SCIMDefinitions.DataType.DATE_TIME;
            } else if (type.equals("decimal")) {
                dataTypeDefinition = SCIMDefinitions.DataType.DECIMAL;
            } else if (type.equals("integer")) {
                dataTypeDefinition = SCIMDefinitions.DataType.INTEGER;
            } else if (type.equals("reference")) {
                dataTypeDefinition = SCIMDefinitions.DataType.REFERENCE;
            } else if (type.equals("string")) {
                dataTypeDefinition = SCIMDefinitions.DataType.STRING;
            }

            if (returned.equals("always")) {
                returnedDefinition = SCIMDefinitions.Returned.ALWAYS;
            } else if (returned.equals("default")) {
                returnedDefinition = SCIMDefinitions.Returned.DEFAULT;
            } else if (returned.equals("never")) {
                returnedDefinition = SCIMDefinitions.Returned.NEVER;
            } else if (returned.equals("request")) {
                returnedDefinition = SCIMDefinitions.Returned.REQUEST;
            }

            if (uniqueness.equals("global")) {
                uniquenessDefinition = SCIMDefinitions.Uniqueness.GLOBAL;
            } else if (uniqueness.equals("none")) {
                uniquenessDefinition = SCIMDefinitions.Uniqueness.NONE;
            } else if (uniqueness.equals("server")) {
                uniquenessDefinition = SCIMDefinitions.Uniqueness.SERVER;
            }

            subTests.add("Validate the attribute definitions of " + attributeName);
            subTests.add("Test description : Check attribute definition follow SCIM specification.");
            subTests.add("Status : Success");
            subTests.add(StringUtils.EMPTY);

            return SCIMAttributeSchema.createSCIMAttributeSchema(uri, name, dataTypeDefinition,
                    multivalued, description, required, caseExact, mutabilityDefinition,
                    returnedDefinition, uniquenessDefinition,
                    null, null, null);

        } catch (JSONException e) {
            subTests.add("Validate the attribute definitions of " + attributeName);
            subTests.add("Test description : Check attribute definition follow SCIM specification.");
            subTests.add("Status : Failed");
            subTests.add(StringUtils.EMPTY);
            throw new CriticalComplianceException(new TestResult
                    (TestResult.ERROR, "Get Schema",
                            e.getMessage() + "in attribute " + attributeName,
                            ComplianceUtils.getWire(method, jsonSchema,
                                    headerString, responseStatus, subTests)));
        }
    }
}
