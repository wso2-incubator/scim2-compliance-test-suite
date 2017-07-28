package info.wso2.scim2.compliance.exception;

import info.wso2.scim2.compliance.entities.TestResult;

public class GeneralComplianceException extends Exception {

	private TestResult result = null;

	private static final long serialVersionUID = 1L;


	public GeneralComplianceException(TestResult result) {
		this.result = result;
	}

	public void setResult(TestResult result) {
		this.result = result;
	}

	public TestResult getResult() {
		return result;
	}
	
}
