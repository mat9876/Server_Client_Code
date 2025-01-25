package Client.messages;

public class FileUploadInfoReq {
    private String fileName;
    private String userName;

    public FileUploadInfoReq(){

    }

    public FileUploadInfoReq(String fileName, String userName) {
        this.fileName = fileName;
        this.userName = userName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUserName() {
        return userName;
    }
}
