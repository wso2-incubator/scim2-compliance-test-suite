package info.wso2.scim2.compliance.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Wire {
    public static final Wire EMPTY = new Wire("<empty>", "<empty>", "<empty>");
    
    @XmlElement(name = "to_server")
    String toServer = "";
    
    @XmlElement(name = "from_server")
    String fromServer = "";

    @XmlElement(name = "tests")
    String tests = "";

    public Wire() {
    }

    public Wire(String toServer, String fromServer, String tests) {
        this.toServer = toServer;
        this.fromServer = fromServer;
        this.tests = tests;
    }


    public String getToServer() {
        return toServer;
    }

    public String getFromServer() {
        return fromServer;
    }

    public String getTests() {
        return tests;
    }
}
