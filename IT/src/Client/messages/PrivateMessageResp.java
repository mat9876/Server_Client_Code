package Client.messages;

public class PrivateMessageResp {
    private String status; // Change "error" to "status"
    private int errorCode; // Change "i" to "errorCode"

    public PrivateMessageResp(String status, int errorCode) {
        this.status = status;
        this.errorCode = errorCode;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
