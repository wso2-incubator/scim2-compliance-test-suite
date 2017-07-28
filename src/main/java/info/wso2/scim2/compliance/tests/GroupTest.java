package info.wso2.scim2.compliance.tests;


import info.wso2.scim2.compliance.entities.TestResult;
import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.exception.GeneralComplianceException;
import info.wso2.scim2.compliance.httpclient.HTTPClient;
import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import info.wso2.scim2.compliance.protocol.ComplianceUtils;
import info.wso2.scim2.compliance.tests.common.ResponseValidateTests;
import info.wso2.scim2.compliance.utils.ComplianceConstants;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.util.ArrayList;

public class GroupTest {

    private ComplianceTestMetaDataHolder complianceTestMetaDataHolder;
    private String url;
    private Group group = null;
    private UserTest userTest = null;

    public GroupTest(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() +
                complianceTestMetaDataHolder.getVersion() +
                ComplianceConstants.TestConstants.GROUPS_ENDPOINT;
    }

    public ArrayList<TestResult> performTest() throws ComplianceException {
        ArrayList<TestResult> testResults = new ArrayList<>();
        try {
            //perform create group test
            testResults.add(CreateGroupTest());
            //perform get group test
            testResults.add(GetGroupTest());
            //perform update group test
            testResults.add(UpdateGroupTest());

            if (complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported()){
                //perform patch group test if and only if it is supported by the SCIM service provider
                testResults.add(PatchGroupTest());
            }
            //perform delete group test
            testResults.add(DeleteGroupTest());

        } catch (GeneralComplianceException e) {
            testResults.add(e.getResult());
        } catch (CharonException e) {
            throw  new ComplianceException(500, "Error in getting the Patch attribute");
        } finally {
            //run clean up task
            RunCleanUpTask();
        }
        return testResults;
    }

    public void RunCleanUpTask() throws ComplianceException {
        try {
            userTest.DeleteUserTest();
        } catch (GeneralComplianceException | ComplianceException e) {
           throw new ComplianceException(500, "Group Clean up task failed");
        }
    }

    public TestResult CreateGroupTest () throws GeneralComplianceException, ComplianceException {
        String definedGroup = null;
        userTest = new UserTest(complianceTestMetaDataHolder);
        try {
            userTest.CreateUserTest();
            definedGroup = "{\"displayName\": \"engineer\", " +
                            "\"members\": " +
                                "[{\"value\":\""+ userTest.getUser().getId() +"\"," +
                                "\"display\": \""+ userTest.getUser().getUserName() +"\"}" +
                            "]}";
        } catch (Exception e) {
            throw new ComplianceException(500, "Error while creating the user to add to group");
        }
        HttpPost method = new HttpPost(url);
        //create group test
        HttpClient client = HTTPClient.getHttpClientWithBasicAuth();

        method = (HttpPost) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");
        method.setHeader("Content-Type", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //create the group
            HttpEntity entity = new ByteArrayEntity(definedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            // Read the response body.
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Group",
                    "Could not create default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 201) {
            //obtain the schema corresponding to group
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group)jsonDecoder.decodeResource(responseString, schema, new Group());
            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema,null, null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Create Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Create Group",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Create Group",
                            "", ComplianceUtils.getWire(method, responseString,
                            headerString, responseStatus, subTests));
        }
    }

    public TestResult GetGroupTest () throws GeneralComplianceException, ComplianceException {

        String getGroupURL = null;
        try {
            getGroupURL = url + "/" + group.getId();
        } catch (CharonException e) {
            throw new ComplianceException(e.getStatus(),"Error in reading the id of the created group.");
        }
        HttpGet method = new HttpGet(getGroupURL);

        HttpClient client = HTTPClient.getHttpClientWithBasicAuth();

        method = (HttpGet) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
           // get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Group",
                    "Could not get the default group from url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to group
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Get Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Get Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Get Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    public TestResult UpdateGroupTest () throws GeneralComplianceException, ComplianceException {

        String updateUserURL = null;
        String definedUpdatedGroup = null;
        try {
            definedUpdatedGroup = "{\"displayName\": \"Doctors\", " +
                    "\"members\": " +
                    "[{\"value\":\""+ userTest.getUser().getId() +"\"," +
                    "\"display\": \""+ userTest.getUser().getUserName() +"\"}" +
                    "]}";
        } catch (Exception e) {
            throw new ComplianceException(500, "Error while getting the user to add to group");
        }
        try {
            updateUserURL = url + "/" + group.getId();
        } catch (CharonException e) {
            throw new ComplianceException(e.getStatus(),"Error in reading the id of the created group.");
        }
        HttpPut method = new HttpPut(updateUserURL);

        HttpClient client = HTTPClient.getHttpClientWithBasicAuth();

        method = (HttpPut) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //update the user
            HttpEntity entity = new ByteArrayEntity(definedUpdatedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");

            response = client.execute(method);

            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Group",
                    "Could not update the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Update Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Update Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Update Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    public TestResult PatchGroupTest () throws GeneralComplianceException, ComplianceException {

        String patchGroupURL = null;
        String definedPatchedGroup = null;
        try {
            definedPatchedGroup = "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
                    "\"Operations\":[{\"op\":\"add\",\"value\":{\"displayName\": \"Actors\"}}]}";
        } catch (Exception e) {
            throw new ComplianceException(500, "Error while getting the user to add to group");
        }
        try {
            patchGroupURL = url + "/" + group.getId();
        } catch (CharonException e) {
            throw new ComplianceException(e.getStatus(),"Error in reading the id of the created group.");
        }
        HttpPatch method = new HttpPatch(patchGroupURL);

        HttpClient client = HTTPClient.getHttpClientWithBasicAuth();

        method = (HttpPatch) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {
            //update the user
            HttpEntity entity = new ByteArrayEntity(definedPatchedGroup.getBytes("UTF-8"));
            method.setEntity(entity);
            method.setHeader("Accept", "application/json");
            method.setHeader("Content-Type", "application/json");

            response = client.execute(method);

            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Group",
                    "Could not patch the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            //obtain the schema corresponding to user
            // unless configured returns core-user schema or else returns extended user schema)
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();

            JSONDecoder jsonDecoder = new JSONDecoder();
            try {
                group = (Group) jsonDecoder.decodeResource(responseString, schema, new Group());

            } catch (BadRequestException | CharonException | InternalErrorException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Group",
                        "Could not decode the server response",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            try {
                ResponseValidateTests.runValidateTests(group, schema, null,
                        null, method,
                        responseString, headerString, responseStatus, subTests);

            } catch (BadRequestException | CharonException e) {
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Patch Group",
                        "Response Validation Error",
                        ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
            }
            return new TestResult
                    (TestResult.SUCCESS, "Patch Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Patch Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }

    public TestResult DeleteGroupTest () throws GeneralComplianceException, ComplianceException {

        String deleteGroupURL = null;
        try {
            deleteGroupURL = url + "/" + group.getId();
        } catch (CharonException e) {
            throw new ComplianceException(e.getStatus(),"Error in reading the id of the created group.");
        }
        HttpDelete method = new HttpDelete(deleteGroupURL);

        HttpClient client = HTTPClient.getHttpClientWithBasicAuth();

        method = (HttpDelete) HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader("Accept", "application/json");

        HttpResponse response = null;
        String responseString = "";
        String headerString = "";
        String responseStatus = "";
        ArrayList<String> subTests =  new ArrayList<>();
        try {

            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();

        } catch (Exception e) {
            // Read the response body.
            //get all headers
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString += header.getName() + " : " + header.getValue() + "\n";
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "Delete Group",
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString, responseStatus, subTests)));
        }

        if (response.getStatusLine().getStatusCode() == 204) {
            return new TestResult
                    (TestResult.SUCCESS, "Delete Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        } else {
            return new TestResult
                    (TestResult.ERROR, "Delete Group",
                            "", ComplianceUtils.getWire(method, responseString, headerString,
                            responseStatus, subTests));
        }
    }


}
