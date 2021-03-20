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
package org.wso2.scim2.testsuite.core.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistics {

    @XmlElement(name = "success")
    int success = 0;

    @XmlElement(name = "failed")
    int failed = 0;

    @XmlElement(name = "skipped")
    int skipped = 0;

    @XmlElement(name = "time")
    long time = 0;

    public Statistics() {

    }

    public void incSkipped() {

        this.skipped++;
    }

    public void incSuccess() {

        this.success++;
    }

    public void incFailed() {

        this.failed++;
    }

    public void setTime(long time) {

        this.time = time;
    }

    public int getSuccess() {

        return success;
    }

    public int getFailed() {

        return failed;
    }

    public int getSkipped() {

        return skipped;
    }

    public long getTime() {

        return time;
    }

    public int getTotal() {

        return (success + skipped + failed);
    }
}
