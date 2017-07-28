package info.wso2.scim2.compliance.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Result {

    @XmlElement(name = "authRequired")
    boolean          authRequired = false;

    @XmlElement(name = "authMethods")
    List<AuthMetod>  authMethods  = new ArrayList<AuthMetod>();

    @XmlElement(name = "results")
    List<TestResult> results      = new ArrayList<TestResult>();

    @XmlElement(name = "statistics")
    Statistics       statistics;

    @XmlElement(name = "error_message")
    String           errorMessage;

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
}
