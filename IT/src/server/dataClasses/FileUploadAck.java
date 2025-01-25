package server.dataClasses;

public class FileUploadAck {
    private String status;  // "ACCEPT" or "REJECT"
    private String from;    // The username of the client sending the ack

    public FileUploadAck() {
    }

    public FileUploadAck(String status, String username) {
        this.status = status;
        this.from = from;
    }

    public String getStatus() {
        return status;
    }

    public String getFrom() {
        return from;
    }
}
