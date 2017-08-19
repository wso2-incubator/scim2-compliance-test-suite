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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Result {

    @XmlElement(name = "results")
    List<TestResult> results      = new ArrayList<TestResult>();

    @XmlElement(name = "statistics")
    Statistics       statistics;

    @XmlElement(name = "errorMessage")
    String           errorMessage = "";

    //@XmlElement(name = "reportLink")
   // String           reportLink = "";

    public Result() {

    }

    public Result(Statistics statistics, List<TestResult> results) {
        this.statistics = statistics;
        this.results = results;
    }

    public Result(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<TestResult> getResults() {
        return results;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

   // public String getReportLink() {
       // return reportLink;
    //}

   // public void setReportLink(String reportLink) {
     //   this.reportLink = reportLink;
   // }
}
