/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.wso2.scim2.compliance.entities;

import info.wso2.scim2.compliance.exception.ComplianceException;
import info.wso2.scim2.compliance.utils.ComplianceConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestResult {

    private static String[] statusLabels = new String[] { ComplianceConstants.TestConstants.LABEL_IMPORTANT,
            ComplianceConstants.TestConstants.LABEL_SUCCESS, ComplianceConstants.TestConstants.LABEL_INFO};

    private static String[] statusTexts  = new String[] { ComplianceConstants.TestConstants.FAILED,
            ComplianceConstants.TestConstants.SUCCESS, ComplianceConstants.TestConstants.SKIPPED };

    public static final int ERROR = 0;
    public static final int SUCCESS  = 1;
    public static final int SKIPPED = 2;

    @XmlElement(name = "name")
    String name = "";

    @XmlElement(name = "message")
    String message = "";

    @XmlElement(name = "status_text")
    String statusText = "Failed";

    @XmlElement(name = "status_label")
    String statusLabel = "label-important";

    @XmlElement(name = "wire")
    Wire wire;

    private int status;

    public TestResult() {

    }

    public TestResult(int status, String name, String message, Wire wire) {
        this.name = name;
        this.message = message;
        this.wire = wire;
        this.status = status;

        this.statusText = statusTexts[status];
        this.statusLabel = statusLabels[status];
    }

    public TestResult(int status, ComplianceException complianceException){
        this.name = complianceException.getDetail();
        this.message = "";
        this.wire = null;
        this.status = status;

        this.statusText = statusTexts[status];
        this.statusLabel = statusLabels[status];
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return message;
    }

    public Wire getWire() {
        return wire;
    }
}
