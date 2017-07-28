package info.wso2.scim2.compliance.httpclient;


import info.wso2.scim2.compliance.protocol.ComplianceTestMetaDataHolder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.Charset;

public class HTTPClient {

    private static HttpClient httpClient = null;

    public static HttpClient getHttpClientWithBasicAuth() {
        if(httpClient == null) {
            HttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
            return httpClient;
        }
        return httpClient;
    }

    public static HttpRequestBase setAuthorizationHeader (ComplianceTestMetaDataHolder complianceTestMetaDataHolder,
                                                          HttpRequestBase method) {

        String auth = complianceTestMetaDataHolder.getUsername() + ":" + complianceTestMetaDataHolder.getPassword();
        if (!auth.equals(":")) {
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            method.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return method;
    }

}


