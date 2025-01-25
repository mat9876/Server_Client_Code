package server;
public class FileUploadRequest {
    private String recipient;
    private String fileName;

    public FileUploadRequest(){

    }

    public FileUploadRequest(String fileName, String recipient){
        this.fileName = fileName;
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
