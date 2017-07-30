/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package info.wso2.scim2.compliance.exception;


public class ComplianceException extends Exception {

    //A detailed human-readable message.
    protected String detail;

    //The HTTP status code
    protected int status;

    public ComplianceException(int status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause. Note that the detail message associated with
     * causeis not automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A null value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public ComplianceException(String message, Throwable cause) {
        super(message, cause);
        this.detail = message;
    }

    public ComplianceException() {

    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
