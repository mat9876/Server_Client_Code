package server.dataClasses;

public class ChallengeRequest{
    private String senderUsername; //The challenger.
    private String targetUsername; //The user being challenged.

    /**
     *Empty constructor needed to handle the JSON implementation.
     **/
    public ChallengeRequest() { }

    /**
     *Constructor used to determine the sender and recipient of a challenge.
     *@param senderUsername; The username of the sender.
     *@param targetUsername; The recipient of the challenge.
     **/
    public ChallengeRequest(String targetUsername, String senderUsername) {
        this.senderUsername = senderUsername;
        this.targetUsername = targetUsername;
    }

    /**
     *Getter for the username variable.
     *@return username; The username of the sender of the challenger
     **/
    public String getUsername() {
        return senderUsername;
    }

    /**
     *Setter for the sender' username.
     *@param username; The username to be changed.
     **/
    public void setUsername(String username) {
        this.senderUsername = username;
    }

    /**
     *Getter for the target's username variable.
     *@return username; The username of the target of the challenger
     **/
    public String getTargetUsername() {
        return targetUsername;
    }

    /**
     *Setter for the recipients' username.
     *@param targetUsername; The username to be changed.
     **/
    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
}