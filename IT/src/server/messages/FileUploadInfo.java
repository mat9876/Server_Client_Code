package server.messages;

public class FileUploadInfo {
    private String status;
    private String message;
    private int code; // Optional field for error code

    // Default constructor for deserialization
    public FileUploadInfo() {}

    public FileUploadInfo(int code){
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
