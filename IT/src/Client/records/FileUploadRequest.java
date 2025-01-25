package Client.records;
public class FileUploadRequest {
    private String recipient;
    private String fileName;
    private String sender;

    public FileUploadRequest(){

    }

    public FileUploadRequest(String fileName, String recipient, String sender){
        this.fileName = fileName;
        this.recipient = recipient;
        this.sender = sender;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
