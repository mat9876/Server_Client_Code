package server.dataClasses;

public class ChallengeResponse {
    private String status; // "OK" for accept, "ERROR" for reject
    private int messageCode;
    private String sender;
    private String receiver;

    //Empty constructor needed to handle the JSON-implementation.
    public ChallengeResponse() { }

    public ChallengeResponse(String status) {
        this.status = status;
    }

    public ChallengeResponse(String status, int messageCode) {
        this.status = status;
        this.messageCode = messageCode;
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

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

}
