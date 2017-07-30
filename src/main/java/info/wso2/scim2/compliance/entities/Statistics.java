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
package info.wso2.scim2.compliance.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistics {
    @XmlElement(name = "success")
    int success = 0;

    @XmlElement(name = "failed")
    int failed  = 0;
    
    @XmlElement(name = "skipped")
    int skipped  = 0;

    public Statistics() {
    }

    public void incSkipped() {
        this.skipped++;
    }

    public void incSuccess() {
        this.success++;
    }

    public void incFailed() { this.failed++; }
}
