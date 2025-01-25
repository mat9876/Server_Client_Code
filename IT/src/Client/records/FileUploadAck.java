package Client.records;

public class FileUploadAck {
    private String status;
    private String from;

    public FileUploadAck(String status, String from) {
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
