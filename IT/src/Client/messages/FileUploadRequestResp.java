package Client.messages;

public record FileUploadRequestResp(String status, String from, String recipient, String fileName, long fileSize) {
}
