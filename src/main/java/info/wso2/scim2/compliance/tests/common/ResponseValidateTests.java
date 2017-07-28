package info.wso2.scim2.compliance.tests.common;

import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.GeneralComplianceException;
import info.wso2.scim2.compliance.protocol.ComplianceUtils;
import info.wso2.scim2.compliance.utils.ComplianceConstants;
import org.apache.http.client.methods.HttpRequestBase;
import org.wso2.charon3.core.attributes.AbstractAttribute;
import org.wso2.charon3.core.attributes.Attribute;
import org.wso2.charon3.core.attributes.ComplexAttribute;
import org.wso2.charon3.core.attributes.MultiValuedAttribute;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.objects.AbstractSCIMObject;
import org.wso2.charon3.core.objects.SCIMObject;
import org.wso2.charon3.core.schema.AttributeSchema;
import org.wso2.charon3.core.schema.SCIMAttributeSchema;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResponseValidateTests {

    public static void runValidateTests(SCIMObject scimObject,
                                        SCIMResourceTypeSchema schema,
                                        String requestedAttributes,
                                        String requestedExcludingAttributes,
                                        HttpRequestBase method,
                                        String responseString,
                                        String headerString,
                                        String responseStatus,
                                        ArrayList<String> subTests)
            throws BadRequestException, CharonException, GeneralComplianceException, ComplianceException {
        //Check for required attributes
        if (!subTests.contains(ComplianceConstants.TestConstants.REQUIRED_ATTRIBUTE_TEST)) {
            subTests.add(ComplianceConstants.TestConstants.REQUIRED_ATTRIBUTE_TEST);
        }
        validateSCIMObjectForRequiredAttributes(scimObject, schema,
                method, responseString, headerString, responseStatus, subTests);

        //validate schema list
        if (!subTests.contains(ComplianceConstants.TestConstants.SCHEMA_LIST_TEST)) {
            subTests.add(ComplianceConstants.TestConstants.SCHEMA_LIST_TEST);
        }
        validateSchemaList(scimObject, schema, method, responseString, headerString, responseStatus, subTests);

        if (!subTests.contains(ComplianceConstants.TestConstants.ATTRIBUTE_MUTABILITY_TEST)) {
            subTests.add(ComplianceConstants.TestConstants.ATTRIBUTE_MUTABILITY_TEST);
        }
        validateReturnedAttributes((AbstractSCIMObject) scimObject, requestedAttributes,
                requestedExcludingAttributes, method,
                responseString, headerString, responseStatus, subTests);

    }

    /*
     * Validate SCIMObject for required attributes given the object and the corresponding schema.
     *
     * @param scimObject
     * @param resourceSchema
     */
    private static void validateSCIMObjectForRequiredAttributes(SCIMObject scimObject,
                                                                SCIMResourceTypeSchema resourceSchema,
                                                                HttpRequestBase method,
                                                                String responseString,
                                                                String headerString,
                                                                String responseStatus,
                                                                ArrayList<String> subTests)
            throws BadRequestException, CharonException, GeneralComplianceException, ComplianceException {
        //get attributes from schema.
        List<AttributeSchema> attributeSchemaList = resourceSchema.getAttributesList();
        //get attribute list from scim object.
        Map<String, Attribute> attributeList = scimObject.getAttributeList();
        for (AttributeSchema attributeSchema : attributeSchemaList) {
            //check for required attributes.
            if (attributeSchema.getRequired()) {
                if (!attributeList.containsKey(attributeSchema.getName())) {
                    String error = "Required attribute " + attributeSchema.getName() + " is missing in the SCIM " +
                            "Object.";
                    throw new GeneralComplianceException
                            (new TestResult(TestResult.ERROR, "Required Attribute Test",
                            error, ComplianceUtils.getWire(method, responseString,
                                    headerString, responseStatus, subTests)));
                }
            }
            //check for required sub attributes.
            AbstractAttribute attribute = (AbstractAttribute) attributeList.get(attributeSchema.getName());
            validateSCIMObjectForRequiredSubAttributes(attribute, attributeSchema,
                    method, responseString, headerString, responseStatus, subTests);
        }
    }

    /*
         * Validate SCIMObject for required sub attributes given the object and the corresponding schema.
         *
         * @param attribute
         * @param attributeSchema
         * @throws CharonException
         * @throws BadRequestException
         */
    private static void validateSCIMObjectForRequiredSubAttributes(AbstractAttribute attribute,
                                                                   AttributeSchema attributeSchema,
                                                                   HttpRequestBase method,
                                                                   String responseString,
                                                                   String headerString,
                                                                   String responseStatus,
                                                                   ArrayList<String> subTests)
            throws GeneralComplianceException, CharonException, ComplianceException {
        if (attribute != null) {
            List<SCIMAttributeSchema> subAttributesSchemaList =
                    ((SCIMAttributeSchema) attributeSchema).getSubAttributeSchemas();

            if (subAttributesSchemaList != null) {
                for (SCIMAttributeSchema subAttributeSchema : subAttributesSchemaList) {
                    if (subAttributeSchema.getRequired()) {

                        if (attribute instanceof ComplexAttribute) {
                            if (attribute.getSubAttribute(subAttributeSchema.getName()) == null) {
                                String error = "Required sub attribute: " + subAttributeSchema.getName()
                                        + " is missing in the SCIM Attribute: " + attribute.getName();
                                throw new GeneralComplianceException
                                        (new TestResult(TestResult.ERROR, "Required Attribute Test",
                                        error, ComplianceUtils.getWire(method, responseString, headerString,
                                        responseStatus, subTests)));
                            }
                        } else if (attribute instanceof MultiValuedAttribute) {
                            List<Attribute> values =
                                    ((MultiValuedAttribute) attribute).getAttributeValues();
                            for (Attribute value : values) {
                                if (value instanceof ComplexAttribute) {
                                    if (value.getSubAttribute(subAttributeSchema.getName()) == null) {
                                        String error = "Required sub attribute: " + subAttributeSchema.getName()
                                                + ", is missing in the SCIM Attribute: " + attribute.getName();
                                        throw new GeneralComplianceException
                                                (new TestResult(TestResult.ERROR, "Required Attribute Test",
                                                error, ComplianceUtils.getWire(method, responseString,
                                                headerString, responseStatus, subTests)));
                                    }
                                }
                            }
                        }
                    }
                    //Following is only applicable for extension schema validation.
                    AbstractAttribute subAttribute = null;
                    if (attribute instanceof ComplexAttribute) {
                        subAttribute = (AbstractAttribute) ((ComplexAttribute) attribute).getSubAttribute
                                (subAttributeSchema.getName());
                    } else if (attribute instanceof MultiValuedAttribute) {
                        List<Attribute> subAttributeList = ((MultiValuedAttribute) attribute).getAttributeValues();
                        for (Attribute subAttrbte : subAttributeList) {
                            if (subAttrbte.getName().equals(subAttributeSchema.getName())) {
                                subAttribute = (AbstractAttribute) subAttrbte;
                            }
                        }
                    }
                    List<SCIMAttributeSchema> subSubAttributesSchemaList =
                            ((SCIMAttributeSchema) subAttributeSchema).getSubAttributeSchemas();
                    if (subSubAttributesSchemaList != null) {
                        validateSCIMObjectForRequiredSubAttributes(subAttribute, subAttributeSchema,
                                method, responseString, headerString, responseStatus, subTests);
                    }
                }
            }
        }
    }

    /*
     * Validate SCIMObject for schema list
     *
     * @param scimObject
     * @param resourceSchema
     */
    public static void validateSchemaList(SCIMObject scimObject,
                                          SCIMResourceTypeSchema resourceSchema,
                                          HttpRequestBase method,
                                          String responseString,
                                          String headerString,
                                          String responseStatus,
                                          ArrayList<String> subTests)
            throws GeneralComplianceException, ComplianceException {

        //get resource schema list
        List<String> resourceSchemaList = resourceSchema.getSchemasList();
        //get the scim object schema list
        List<String> objectSchemaList = scimObject.getSchemaList();

        for (String schema : resourceSchemaList) {
            //check for schema.
            if (!objectSchemaList.contains(schema)) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Schema List Test",
                        "Not all schemas are set", ComplianceUtils.getWire(method,
                        responseString, headerString, responseStatus, subTests)));
            }
        }
    }

    /*
    * This method is to remove any defined and requested attributes and include
    * requested attributes if not they have been removed.
    *
    * @param scimObject
    * @param requestedAttributes
    * @param requestedExcludingAttributes
    */
    public static void validateReturnedAttributes(AbstractSCIMObject scimObject,
                                                  String requestedAttributes,
                                                  String requestedExcludingAttributes,
                                                  HttpRequestBase method,
                                                  String responseString,
                                                  String headerString,
                                                  String responseStatus,
                                                  ArrayList<String> subTests) throws GeneralComplianceException, ComplianceException {
        List<String> requestedAttributesList = null;
        List<String> requestedExcludingAttributesList = null;

        if (requestedAttributes != null) {
            //make a list from the comma separated requestedAttributes
            requestedAttributesList = Arrays.asList(requestedAttributes.split(","));
        }
        if (requestedExcludingAttributes != null) {
            //make a list from the comma separated requestedExcludingAttributes
            requestedExcludingAttributesList = Arrays.asList(requestedExcludingAttributes.split(","));
        }
        Map<String, Attribute> attributeList = scimObject.getAttributeList();
        ArrayList<Attribute> attributeTemporyList = new ArrayList<Attribute>();
        for (Attribute attribute : attributeList.values()) {
            attributeTemporyList.add(attribute);
        }
        for (Attribute attribute : attributeTemporyList) {
            //check for never/request attributes.
            if (attribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                        "Attribute : " + attribute.getName() + " violates mutability condition.",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            //if the returned property is request, need to check whether is it specifically requested by the user.
            // If so return it.
            if (requestedAttributes == null && requestedExcludingAttributes == null) {
                if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                    scimObject.deleteAttribute(attribute.getName());
                }
            } else {
                //A request should only contains either attributes or exclude attribute params. Not both
                if (requestedAttributes != null) {
                    //if attributes are set, delete all the request and default attributes
                    //and add only the requested attributes
                    if ((attribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                            || attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                            && (!requestedAttributesList.contains(attribute.getName())
                            && !isSubAttributeExistsInList(requestedAttributesList, attribute))) {
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                                "Attribute : " + attribute.getName() + " violates mutability condition.",
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    }
                } else if (requestedExcludingAttributes != null) {
                    //removing attributes which has returned as request. This is because no request is made
                    if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                                "Attribute : " + attribute.getName() + " violates mutability condition.",
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    }
                    //if exclude attribute is set, set of exclude attributes need to be
                    // removed from the default set of attributes
                    if ((attribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                            && requestedExcludingAttributesList.contains(attribute.getName())) {
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                                "Attribute : " + attribute.getName() + " violates mutability condition.",
                                ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                    }
                }
            }
            // If the Returned type ALWAYS : no need to check and it will be not affected by
            // requestedExcludingAttributes parameter

            //check the same for sub attributes
            if (attribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
                if (attribute.getMultiValued()) {
                    List<Attribute> valuesList = ((MultiValuedAttribute) attribute).getAttributeValues();

                    for (Attribute subAttribute : valuesList) {
                        Map<String, Attribute> valuesSubAttributeList = ((ComplexAttribute) subAttribute)
                                .getSubAttributesList();
                        ArrayList<Attribute> valuesSubAttributeTemporyList = new ArrayList<Attribute>();
                        //as we are deleting the attributes form the list, list size will change,
                        //hence need to traverse on a copy
                        for (Attribute subSimpleAttribute : valuesSubAttributeList.values()) {
                            valuesSubAttributeTemporyList.add(subSimpleAttribute);
                        }
                        for (Attribute subSimpleAttribute : valuesSubAttributeTemporyList) {
                            removeValuesSubAttributeOnReturn(subSimpleAttribute, subAttribute, attribute,
                                    requestedAttributes, requestedExcludingAttributes, requestedAttributesList,
                                    requestedExcludingAttributesList, method,
                                    responseString, headerString, responseStatus, subTests);
                        }
                    }
                } else {
                    Map<String, Attribute> subAttributeList = ((ComplexAttribute) attribute).getSubAttributesList();
                    ArrayList<Attribute> subAttributeTemporyList = new ArrayList<Attribute>();
                    for (Attribute subAttribute : subAttributeList.values()) {
                        subAttributeTemporyList.add(subAttribute);
                    }
                    for (Attribute subAttribute : subAttributeTemporyList) {
                        if (subAttribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
                            //this applicable for extension schema only
                            if (subAttribute.getMultiValued()) {

                                List<Attribute> valuesList = ((MultiValuedAttribute) subAttribute).getAttributeValues();

                                for (Attribute subSubValue : valuesList) {
                                    Map<String, Attribute> subValuesSubAttributeList = ((ComplexAttribute)
                                            subSubValue).getSubAttributesList();
                                    ArrayList<Attribute> valuesSubSubAttributeTemporyList = new ArrayList<Attribute>();
                                    //as we are deleting the attributes form the list, list size will change,
                                    //hence need to traverse on a copy
                                    for (Attribute subSubSimpleAttribute : subValuesSubAttributeList.values()) {
                                        valuesSubSubAttributeTemporyList.add(subSubSimpleAttribute);
                                    }
                                    for (Attribute subSubSimpleAttribute : valuesSubSubAttributeTemporyList) {
                                        removeValuesSubSubAttributeOnReturn(attribute, subAttribute, subSubValue,
                                                subSubSimpleAttribute,
                                                requestedAttributes, requestedExcludingAttributes,
                                                requestedAttributesList, requestedExcludingAttributesList,
                                                method, responseString, headerString, responseStatus, subTests);
                                    }
                                }
                            } else {
                                ArrayList<Attribute> subSubAttributeTemporyList = new ArrayList<Attribute>();
                                Map<String, Attribute> subSubAttributeList = ((ComplexAttribute) subAttribute)
                                        .getSubAttributesList();
                                for (Attribute subSubAttribute : subSubAttributeList.values()) {
                                    subSubAttributeTemporyList.add(subSubAttribute);
                                }
                                for (Attribute subSubAttribute : subSubAttributeTemporyList) {
                                    removeSubSubAttributesOnReturn(attribute, subAttribute, subSubAttribute,
                                            requestedAttributes, requestedExcludingAttributes,
                                            requestedAttributesList, requestedExcludingAttributesList,
                                            method, responseString, headerString, responseStatus, subTests);
                                }
                            }
                            removeSubAttributesOnReturn(subAttribute, attribute, requestedAttributes,
                                    requestedExcludingAttributes,
                                    requestedAttributesList, requestedExcludingAttributesList,
                                    method, responseString, headerString, responseStatus, subTests);
                        } else {
                            removeSubAttributesOnReturn(subAttribute, attribute, requestedAttributes,
                                    requestedExcludingAttributes,
                                    requestedAttributesList, requestedExcludingAttributesList,
                                    method, responseString, headerString, responseStatus, subTests);
                        }
                    }
                }
            }
        }
    }

    /*
     * This checks whether, within the 'requestedAttributes', is there a sub attribute of the 'attribute'.
     * If so we should not delete the 'attribute'
     *
     * @param requestedAttributes
     * @param attribute
     * @return boolean
     */
    private static boolean isSubAttributeExistsInList(List<String> requestedAttributes,
                                                      Attribute attribute) {
        List<Attribute> subAttributes = null;
        if (attribute instanceof MultiValuedAttribute) {
            subAttributes =
                    (List<Attribute>) ((MultiValuedAttribute) attribute).getAttributeValues();
            if (subAttributes != null) {
                for (Attribute subAttribute : subAttributes) {
                    ArrayList<Attribute> subSimpleAttributes = new ArrayList<Attribute>((
                            (ComplexAttribute) subAttribute).getSubAttributesList().values());
                    for (Attribute subSimpleAttribute : subSimpleAttributes) {
                        if (requestedAttributes.contains(attribute.getName() + "." + subSimpleAttribute.getName())) {
                            return true;
                        }
                    }
                    //this case is only valid for extension schema
                    if (subAttribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
                        boolean isSubSubAttributeExists = isSubSubAttributeExistsInList(requestedAttributes,
                                attribute, subAttribute);
                        if (isSubSubAttributeExists) {
                            return true;
                        }
                    }
                }
            }
        } else if (attribute instanceof ComplexAttribute) {
            //complex attributes have sub attribute map, hence need conversion to arraylist
            subAttributes = new ArrayList<Attribute>
                    (((Map) (((ComplexAttribute) attribute).getSubAttributesList())).values());
            for (Attribute subAttribute : subAttributes) {
                if (requestedAttributes.contains(attribute.getName() + "." + subAttribute.getName())) {
                    return true;
                }
                //this case is only valid for extension schema
                if (subAttribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
                    boolean isSubSubAttributeExists = isSubSubAttributeExistsInList(requestedAttributes,
                            attribute, subAttribute);
                    if (isSubSubAttributeExists) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * This checks whether, within the 'requestedAttributes', is there a sub attribute of the 'subAttribute'.
     * If so we should not delete the 'attribute'
     * This case is only applicable for extension
     *
     * @param requestedAttributes
     * @param grandParentAttribute
     * @param parentAttribute
     * @return
     */
    private static boolean isSubSubAttributeExistsInList(List<String> requestedAttributes,
                                                         Attribute grandParentAttribute, Attribute parentAttribute) {
        List<Attribute> subAttributes = null;
        if (parentAttribute instanceof MultiValuedAttribute) {
            subAttributes = (List<Attribute>)
                    ((MultiValuedAttribute) parentAttribute).getAttributeValues();
            if (subAttributes != null) {
                for (Attribute subAttribute : subAttributes) {
                    ArrayList<Attribute> subSimpleAttributes = new ArrayList<Attribute>((
                            (ComplexAttribute) subAttribute).getSubAttributesList().values());
                    for (Attribute subSimpleAttribute : subSimpleAttributes) {
                        if (requestedAttributes.contains(grandParentAttribute.getName() + "." +
                                parentAttribute.getName() + "." + subSimpleAttribute.getName())) {
                            return true;
                        }
                    }
                }
            }
        } else if (parentAttribute instanceof ComplexAttribute) {
            //complex attributes have sub attribute map, hence need conversion to arraylist
            subAttributes = new ArrayList<Attribute>
                    (((Map) (((ComplexAttribute) parentAttribute).getSubAttributesList())).values());
            for (Attribute subAttribute : subAttributes) {
                if (requestedAttributes.contains(grandParentAttribute.getName() + "." +
                        parentAttribute.getName() + "." + subAttribute.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * This method is to remove any defined and requested sub attributes and include requested sub attributes
     * from multivalued attributes
     *
     * @param subSimpleAttribute
     * @param subAttribute
     * @param attribute
     * @param requestedAttributes
     * @param requestedExcludingAttributes
     * @param requestedAttributesList
     * @param requestedExcludingAttributesList
     * @param scimObject
     */
    private static void removeValuesSubAttributeOnReturn(Attribute subSimpleAttribute, Attribute subAttribute,
                                                         Attribute attribute, String requestedAttributes,
                                                         String requestedExcludingAttributes,
                                                         List<String> requestedAttributesList,
                                                         List<String> requestedExcludingAttributesList,
                                                         HttpRequestBase method,
                                                         String responseString,
                                                         String headerString,
                                                         String responseStatus,
                                                         ArrayList<String> subTests) throws GeneralComplianceException, ComplianceException {

        if (subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                    "Attribute : " + attribute.getName()+ "." +
                            subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
        if (requestedAttributes == null && requestedExcludingAttributes == null) {
            if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                        "Attribute : " + attribute.getName()+ "." +
                                subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        } else {
            //A request should only contains either attributes or exclude attribute params. Not the both
            if (requestedAttributes != null) {
                //if attributes are set, delete all the request and default attributes
                // and add only the requested attributes
                if ((subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                        || subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                        && (!requestedAttributesList.contains(
                        attribute.getName() + "." + subSimpleAttribute.getName()) &&
                        !requestedAttributesList.contains(attribute.getName()) &&
                        !isSubSubAttributeExistsInList(requestedAttributesList, attribute, subSimpleAttribute))) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            } else if (requestedExcludingAttributes != null) {
                //removing attributes which has returned as request. This is because no request is made
                if (subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                //if exclude attribute is set, set of exclude attributes need to be
                // removed from the default set of attributes
                if ((subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                        && requestedExcludingAttributesList.contains(
                        attribute.getName() + "." + subSimpleAttribute.getName())) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            }
        }


    }

    /*
     * This method is to remove any defined and requested sub attributes and include requested sub attributes
     * from multivalued attributes
     * <p>
     * This is only applicable for extension schema
     *
     * @param attribute
     * @param subAttribute
     * @param subValue
     * @param subSimpleAttribute
     * @param requestedAttributes
     * @param requestedExcludingAttributes
     * @param requestedAttributesList
     * @param requestedExcludingAttributesList
     * @param scimObject
     */
    private static void removeValuesSubSubAttributeOnReturn(Attribute attribute, Attribute subAttribute, Attribute
            subValue,
                                                            Attribute subSimpleAttribute,
                                                            String requestedAttributes,
                                                            String requestedExcludingAttributes,
                                                            List<String> requestedAttributesList,
                                                            List<String> requestedExcludingAttributesList,
                                                            HttpRequestBase method,
                                                            String responseString,
                                                            String headerString,
                                                            String responseStatus,
                                                            ArrayList<String> subTests) throws GeneralComplianceException, ComplianceException {


        if (subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                    "Attribute : " + attribute.getName()+ "." +
                            subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
        if (requestedAttributes == null && requestedExcludingAttributes == null) {
            if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                        "Attribute : " + attribute.getName()+ "." +
                                subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        } else {
            //A request should only contains either attributes or exclude attribute params. Not the both
            if (requestedAttributes != null) {
                //if attributes are set, delete all the request and default attributes
                // and add only the requested attributes
                if ((subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                        || subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                        && (!requestedAttributesList.contains(
                        attribute.getName() + "." + subAttribute.getName() + "." + subSimpleAttribute.getName()) &&
                        !requestedAttributesList.contains(attribute.getName()) &&
                        !requestedAttributesList.contains(attribute.getName() + "." + subAttribute.getName()))) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            } else if (requestedExcludingAttributes != null) {
                //removing attributes which has returned as request. This is because no request is made
                if (subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                //if exclude attribute is set, set of exclude attributes need to be
                // removed from the default set of attributes
                if ((subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                        && requestedExcludingAttributesList.contains(
                        attribute.getName() + "." + subAttribute.getName() + "." + subSimpleAttribute.getName())) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + "." + subSimpleAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            }
        }
    }

    /*
     * This method is to remove any defined and requested sub attributes and include requested sub attributes
     * from complex attributes.
     * <p>
     * This is applicable for extension schema only
     *
     * @param attribute
     * @param subAttribute
     * @param subSubAttribute
     * @param requestedAttributes
     * @param requestedExcludingAttributes
     * @param requestedAttributesList
     * @param requestedExcludingAttributesList
     * @param scimObject
     * @throws CharonException
     */
    private static void removeSubSubAttributesOnReturn(Attribute attribute,
                                                       Attribute subAttribute,
                                                       Attribute subSubAttribute,
                                                       String requestedAttributes,
                                                       String requestedExcludingAttributes,
                                                       List<String> requestedAttributesList,
                                                       List<String> requestedExcludingAttributesList,
                                                       HttpRequestBase method,
                                                       String responseString,
                                                       String headerString,
                                                       String responseStatus,
                                                       ArrayList<String> subTests) throws GeneralComplianceException, ComplianceException {
        //check for never/request attributes.
        if (subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                    "Attribute : " + attribute.getName()+ "." +
                            subAttribute.getName() + " violates mutability condition.",
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
        //if the returned property is request, need to check whether is it specifically requested by the user.
        // If so return it.
        if (requestedAttributes == null && requestedExcludingAttributes == null) {
            if (subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                        "Attribute : " + attribute.getName()+ "." +
                                subAttribute.getName() + " violates mutability condition.",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        } else {
            //A request should only contains either attributes or exclude attribute params. Not the both
            if (requestedAttributes != null) {
                //if attributes are set, delete all the request and default attributes
                // and add only the requested attributes
                if ((subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                        || subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                        && (!requestedAttributesList.contains(
                        attribute.getName() + "." + subAttribute.getName() + "." + subSubAttribute.getName()) &&
                        !requestedAttributesList.contains(attribute.getName()) &&
                        !requestedAttributesList.contains(attribute.getName() + "." + subAttribute.getName()) &&
                        !subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.ALWAYS))) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            } else if (requestedExcludingAttributes != null) {
                //removing attributes which has returned as request. This is because no request is made
                if (subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                //if exclude attribute is set, set of exclude attributes need to be
                // removed from the default set of attributes
                if ((subSubAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                        && requestedExcludingAttributesList.contains(
                        attribute.getName() + "." + subAttribute.getName() + "." + subSubAttribute.getName())) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            }
        }
    }

    /*
     * This method is to remove any defined and requested sub attributes and include requested sub attributes
     * from complex attributes.
     *
     * @param subAttribute
     * @param attribute
     * @param requestedAttributes
     * @param requestedExcludingAttributes
     * @param requestedAttributesList
     * @param requestedExcludingAttributesList
     * @param scimObject
     */

    private static void removeSubAttributesOnReturn(Attribute subAttribute,
                                                    Attribute attribute,
                                                    String requestedAttributes,
                                                    String requestedExcludingAttributes,
                                                    List<String> requestedAttributesList,
                                                    List<String> requestedExcludingAttributesList,
                                                    HttpRequestBase method,
                                                    String responseString,
                                                    String headerString,
                                                    String responseStatus,
                                                    ArrayList<String> subTests) throws GeneralComplianceException, ComplianceException {
        //check for never/request attributes.
        if (subAttribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                    "Attribute : " + attribute.getName()+ "." +
                            subAttribute.getName() + " violates mutability condition.",
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }
        //if the returned property is request, need to check whether is it specifically requested by the user.
        // If so return it.
        if (requestedAttributes == null && requestedExcludingAttributes == null) {
            if (subAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                        "Attribute : " + attribute.getName()+ "." +
                                subAttribute.getName() + " violates mutability condition.",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
        } else {
            //A request should only contains either attributes or exclude attribute params. Not the both
            if (requestedAttributes != null) {
                //if attributes are set, delete all the request and default attributes
                // and add only the requested attributes
                if ((subAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                        || subAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                        && (!requestedAttributesList.contains(
                        attribute.getName() + "." + subAttribute.getName()) &&
                        !requestedAttributesList.contains(attribute.getName()) &&
                        !isSubSubAttributeExistsInList(requestedAttributesList, attribute, subAttribute))) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            } else if (requestedExcludingAttributes != null) {
                //removing attributes which has returned as request. This is because no request is made
                if (subAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
                //if exclude attribute is set, set of exclude attributes need to be
                // removed from the default set of attributes
                if ((subAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                        && requestedExcludingAttributesList.contains(
                        attribute.getName() + "." + subAttribute.getName())) {
                    throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Attribute Mutability Test",
                            "Attribute : " + attribute.getName()+ "." +
                                    subAttribute.getName() + " violates mutability condition.",
                            ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
                }
            }
        }
    }
}
