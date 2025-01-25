package server.messages;

public class FileUploadInfoConfirm {
    private int status;
    private String from;

    public FileUploadInfoConfirm() {
    }

    public FileUploadInfoConfirm(int status, String from) {
        this.status = status;
        this.from = from;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFrom() {
        return from;
    }
}
