package Client.records.dataClasses;

public class ChallengeResponse {
    private String status; // "OK" for accept, "ERROR" for reject
    private int messageCode;
    private String sender;
    private String receiver;

    //Empty constructor needed to handle the JSON-implementation.
    public ChallengeResponse() { }

    public ChallengeResponse(String status, String sender, String receiver) {
        this.status = status;
        this.sender = sender;
        this.receiver = receiver;
    }

    public ChallengeResponse(String status, int messageCode) {
        this.status = status;
        this.messageCode = messageCode;
    }

    public ChallengeResponse(String status, int messageCode, String sender, String receiver) {
        this.status = status;
        this.messageCode = messageCode;
        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     *Getter for the status variable.
     *@return
     **/
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

}
