package info.wso2.scim2.compliance.protocol;

import info.wso2.scim2.compliance.exception.ComplianceException;
import org.apache.commons.lang.exception.ExceptionUtils;
import info.wso2.scim2.compliance.entities.Wire;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

public class ComplianceUtils {

    public static Wire getWire(HttpRequestBase method, String responseBody,
                               String headerString, String responseStatus,
                               ArrayList<String> subTests) throws ComplianceException {

        StringBuffer toServer = new StringBuffer();
        StringBuffer fromServer = new StringBuffer();
        StringBuffer subTestsPerformed = new StringBuffer();

        toServer.append(method.getRequestLine().getMethod()).append(" ");
        toServer.append(method.getRequestLine().getUri()+"\n");
        toServer.append(method.getRequestLine().getProtocolVersion().getProtocol());
        for (org.apache.http.Header header : method.getAllHeaders()) {
            toServer.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }

        if(method.getMethod() != "GET" && method.getMethod() != "DELETE"){
            try {
                HttpEntity entity = ((HttpEntityEnclosingRequest)method).getEntity();
                toServer.append(EntityUtils.toString(entity));
            } catch (Exception e) {
                throw new ComplianceException(500, "Error in getting the request payload");
            }
        }
        fromServer.append("\n" + "Headers : ");
        fromServer.append(headerString + "\n");
        fromServer.append("\n" + "Status : ");
        fromServer.append(responseStatus + "\n");
        fromServer.append("\n" + responseBody);
        for (String subTest : subTests) {
            subTestsPerformed.append(subTest).append("\n");
        }
        return new Wire(toServer.toString(), fromServer.toString(), subTestsPerformed.toString());
    }


    public static Wire getWire(Throwable e) {
        return new Wire(ExceptionUtils.getFullStackTrace(e), "", "");
    }
}
