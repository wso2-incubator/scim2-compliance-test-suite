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
