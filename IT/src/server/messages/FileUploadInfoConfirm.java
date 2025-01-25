package server.messages;

public class FileUploadInfoConfirm {
    private int status;

    public FileUploadInfoConfirm() {
    }

    public FileUploadInfoConfirm(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
