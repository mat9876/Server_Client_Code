package Client.language;
import Client.UsernameClient;

import java.util.*;

public class LanguageManager {
    private String currentLanguage;
    private final Map<String, Map<String, String>> translations;
    private UsernameClient usernameClient;

    public LanguageManager(UsernameClient usernameClient) {
        currentLanguage = "EN"; //Default language is English
        translations = new HashMap<>();
        loadTranslations();
        this.usernameClient = usernameClient;
    }

    private void loadTranslations() {
        Map<String, String> english = new HashMap<>();
        english.put("ConnectingToServer", "Attempting to connect to server...");
        english.put("ConnectToServer", "Connected to server.");
        english.put("LoginMessage", "Enter your username (3-14 characters, no '*'): ");
        english.put("FailedLogin", "Failed to join the server. Exiting.");
        english.put("ErrorServerReading", "Error reading from server: ");
        english.put("InvalidChoice", "Invalid choice. Please select a valid option.");
        english.put("ErrorServerSending", "An error occurred while processing your request: ");
        english.put("FileUploadingRequestReceived", "\nFile upload request received from: ");
        english.put("FileNameDisplay","File name: ");
        english.put("ErrorProcessing", "Error processing file upload request: ");
        english.put("FileAccepted","File upload accepted. File will be downloaded shortly.");
        english.put("ErrorAcceptingFile","Error accepting file upload: ");
        english.put("NoFileToAccept", "No pending file upload request to accept.");
        english.put("FileUploadRejected", "File upload rejected.");
        english.put("NoFileToReject", "No pending file upload request to reject.");
        english.put("EnterRecipientUsername", "Enter the recipient's username: ");
        english.put("EnterFilePath", "Enter the file path: ");
        english.put("ErrorFileNotFound", "Error: File not found at ");
        english.put("FileUploadSuccessful", "File upload request sent successfully.");
        english.put("ChangeLanguage", "Please enter a language: ");
        english.put("menuOptionSelection", "menu: ");
        english.put("Option1", "1. Send a broadcast message");
        english.put("Option2", "2. Send a private message");
        english.put("Option3", "3. Request list of connected users");
        english.put("Option4", "4. Start Rock Paper Scissors");
        english.put("Option5", "5. Request a file upload");
        english.put("Option6", "6. Change language of the client");
        english.put("Option7", "7. Exit");
        english.put("ChooseOption", "Choose an option: ");
        english.put("menuChallengeSelection", "\nChallenge Received:");
        english.put("OptionChallenge1", "1. Accept Challenge");
        english.put("OptionChallenge2", "2. Reject Challenge");
        english.put("menuRPSSelection", "\nYou have received a challenge, please make your selection:");
        english.put("OptionRPS1", "1. Choose Rock");
        english.put("OptionRPS2", "2. Choose Paper");
        english.put("OptionRPS3", "3. Choose Scissors");
        english.put("menuFileSelection", "\nFile Upload Request:");
        english.put("OptionFile1", "1. Accept File");
        english.put("OptionFile2", "2. Reject File");
        english.put("EnterBroadcast", "Enter your broadcast message: ");
        english.put("EnterPrivateRecipient", "Enter recipient's username: ");
        english.put("EnterPrivateMessage", "Enter your message: ");
        english.put("EnterRecipientChallenge", "Enter the username of the user you want to challenge: ");
        english.put("ErrorReceivingChallenge", "Error with challenge response: ");
        english.put("ChallengeResult", "Rock Paper Scissors Result: ");
        english.put("FileUploadAcknowledged", "File upload acknowledged by server. Starting upload for file: ");
        english.put("FileUploadDenied","File upload denied by server: ");
        english.put("FileTransferComplete","Server indicates file transfer completion.");
        english.put("InvalidMessage","Invalid message received from the server: ");
        english.put("InvalidMetaData","No valid file metadata received.");
        english.put("ReceivedFile","Receiving file: ");
        english.put("DownloadPercentage","Downloaded %d/%d bytes (%.2f%%)%n");
        english.put("FileComplete","File reception completed: ");
        english.put("UnexpectedEnd","Unexpected end of file marker: ");
        english.put("ErrorReceivingFile","Error receiving file from server: ");
        english.put("FileNotFound","Error: File not found: " );
        english.put("UploadPercentage","Uploaded %d/%d bytes (%.2f%%)%n");
        english.put("TotalByteSend","File upload completed successfully. Total bytes sent: ");
        english.put("ErrorUploadingFile","Error uploading file to server: ");
        english.put("ClientSent","Client: Sent ");
        english.put("HandleReceivingBroadcast", "Broadcast from ");
        english.put("HandleBroadcastError", "Error parsing BROADCAST message: ");
        english.put("HandleReceivingJoined", " has joined the server.");
        english.put("HandleJoinedError","Error parsing JOINED message: ");
        english.put("HandleConformationSendingBroadcast", "Broadcast message has been send to the server correctly.");
        english.put("InvalidChoiceLanguage", "Known message received without an translation.");
        english.put("MainSocketClosed","Main socket closed.");
        english.put("FileSocketClosed", "File socket closed.");
        english.put("ErrorClosingSocket", "Error closing sockets: ");
        english.put("ClientStopped","Client stopped.");
        english.put("SameNameFile", "You are trying to upload a file to yourself.");
        english.put("Chunk1", "Sent chunk of size: ");
        english.put("Chunk2", " bytes. Total bytes sent: ");
        english.put("ChallengeStart","The challenge has started! Make your move.");
        english.put("MoveSent", "Your move has been sent: ");
        english.put("ErrorSendingMove", "Error sending your move: ");
        english.put("SendBroadcast", "Broadcast message send containing: ");
        english.put("PrivateMessageReceived", "Private message from %s: %s");
        english.put("PrivateMessageSent","Private message sent successfully.");

        Map<String, String> dutch = new HashMap<>();
        dutch.put("ConnectingToServer", "Proberen met de server te verbinden...");
        dutch.put("ConnectToServer", "Verbonden met de server.");
        dutch.put("LoginMessage", "Voer uw gebruikersnaam in (3-14 tekens, geen '*'): ");
        dutch.put("FailedLogin", "Kan niet verbinding maken met de server. Afsluiten.");
        dutch.put("ErrorServerReading", "Fout bij het lezen van de server: ");
        dutch.put("InvalidChoice", "Ongeldige keuze. Kies een geldige optie.");
        dutch.put("ErrorServerSending", "Er is een fout opgetreden bij het verwerken van uw verzoek: ");
        dutch.put("FileUploadingRequestReceived", "\nBestand uploadverzoek ontvangen van: ");
        dutch.put("FileNameDisplay", "Bestandsnaam: ");
        dutch.put("ErrorProcessing", "Fout bij het verwerken van uploadverzoek: ");
        dutch.put("FileAccepted", "Bestand upload geaccepteerd. Bestand wordt binnenkort gedownload.");
        dutch.put("ErrorAcceptingFile", "Fout bij het accepteren van bestand upload: ");
        dutch.put("NoFileToAccept", "Geen openstaand bestand uploadverzoek om te accepteren.");
        dutch.put("FileUploadRejected", "Bestand upload geweigerd.");
        dutch.put("NoFileToReject", "Geen openstaand bestand uploadverzoek om te weigeren.");
        dutch.put("EnterRecipientUsername", "Voer de gebruikersnaam van de ontvanger in: ");
        dutch.put("EnterFilePath", "Voer het bestandspad in: ");
        dutch.put("ErrorFileNotFound", "Fout: Bestand niet gevonden op ");
        dutch.put("FileUploadSuccessful", "Bestand uploadverzoek succesvol verzonden.");
        dutch.put("ChangeLanguage", "Selecteer een taal voor de client: ");
        dutch.put("menuOptionSelection", "menu: ");
        dutch.put("Option1", "1. Stuur een broadcastbericht");
        dutch.put("Option2", "2. Stuur een privébericht");
        dutch.put("Option3", "3. Vraag een lijst op van verbonden gebruikers");
        dutch.put("Option4", "4. Start Steen Papier Schaar");
        dutch.put("Option5", "5. Verzoek om bestand upload");
        dutch.put("Option6", "6. Verander taal van de client");
        dutch.put("Option7", "7. Afsluiten");
        dutch.put("ChooseOption", "Kies een optie: ");
        dutch.put("menuChallengeSelection", "\nUitdaging Ontvangen:");
        dutch.put("OptionChallenge1", "1. Accepteer Uitdaging");
        dutch.put("OptionChallenge2", "2. Weiger Uitdaging");
        dutch.put("menuRPSSelection", "\nU heeft een uitdaging ontvangen, maak uw keuze:");
        dutch.put("OptionRPS1", "1. Kies Steen");
        dutch.put("OptionRPS2", "2. Kies Papier");
        dutch.put("OptionRPS3", "3. Kies Schaar");
        dutch.put("menuFileSelection", "\nBestand Upload Verzoek:");
        dutch.put("OptionFile1", "1. Accepteer Bestand");
        dutch.put("OptionFile2", "2. Weiger Bestand");
        dutch.put("EnterBroadcast", "Voer uw broadcastbericht in: ");
        dutch.put("EnterPrivateRecipient", "Voer de gebruikersnaam van de ontvanger in: ");
        dutch.put("EnterPrivateMessage", "Voer uw bericht in: ");
        dutch.put("EnterRecipientChallenge", "Voer de gebruikersnaam in van de persoon die u wilt uitdagen: ");
        dutch.put("ErrorReceivingChallenge", "Fout met uitdaging reactie: ");
        dutch.put("ChallengeResult", "Steen Papier Schaar Resultaat: ");
        dutch.put("FileUploadAcknowledged", "Bestand upload bevestigd door server. Start upload voor bestand: ");
        dutch.put("FileUploadDenied", "Bestand upload geweigerd door server: ");
        dutch.put("FileTransferComplete", "Server geeft aan dat bestandsoverdracht voltooid is.");
        dutch.put("InvalidMessage", "Ongeldig bericht ontvangen van de server: ");
        dutch.put("InvalidMetaData", "Geen geldige bestand metadata ontvangen.");
        dutch.put("ReceivedFile", "Bestand ontvangen: ");
        dutch.put("DownloadPercentage", "Gedownload %d/%d bytes (%.2f%%)%n");
        dutch.put("FileComplete", "Bestandsontvangst voltooid: ");
        dutch.put("UnexpectedEnd", "Onverwacht einde van bestand marker: ");
        dutch.put("ErrorReceivingFile", "Fout bij ontvangen van bestand van server: ");
        dutch.put("FileNotFound", "Fout: Bestand niet gevonden: ");
        dutch.put("UploadPercentage", "Geüpload %d/%d bytes (%.2f%%)%n");
        dutch.put("TotalByteSend", "Bestand upload succesvol voltooid. Totaal verzonden bytes: ");
        dutch.put("ErrorUploadingFile", "Fout bij uploaden van bestand naar server: ");
        dutch.put("ClientSent", "Client: Verzonden ");
        dutch.put("HandleReceivingBroadcast", "Broadcast-bericht van ");
        dutch.put("HandleBroadcastError", "Foutmeldingen bij het ontvangen van BROADCAST-bericht: ");
        dutch.put("HandleReceivingJoined", " heeft aangesloten op de server.");
        dutch.put("HandleJoinedError","Foutmeldingen bij het ontvangen van JOINED-bericht: ");
        dutch.put("HandleConformationSendingBroadcast", "Broadcast-bericht is correct ontvangen.");
        dutch.put("InvalidChoiceLanguage", "Bekend bericht ontvangen zonder vertaling.");
        dutch.put("MainSocketClosed","Hoofdsocket gesloten.");
        dutch.put("FileSocketClosed", "Bestandsocket gesloten.");
        dutch.put("ErrorClosingSocket", "Fout tijdens sluiten van sockets: ");
        dutch.put("ClientStopped","Client gestopt.");
        dutch.put("SameNameFile", "Je probeert een bestand naar jezelf te sturen.");
        dutch.put("Chunk1", "Chunk verstuurd van grootten: ");
        dutch.put("Chunk2", " bytes. Totalen bytes verstuurd: ");
        dutch.put("MoveSent", "Uw keuze is verzonden: ");
        dutch.put("ChallengeStart", "Uitdaging gestart, maak je selectie: ");
        dutch.put("ErrorSendingMove", "Foutmeldingen met bericht sturen: ");
        dutch.put("SendBroadcast", "Broadcast-bericht gestuurd met inhoud: ");
        dutch.put("PrivateMessageReceived", "Prive-bericht van %s: %s");
        dutch.put("PrivateMessageSent","Prive-bericht verzonden.");

        translations.put("EN", english);
        translations.put("NL", dutch);
    }

    public void setLanguage(String languageCode) {
        if (translations.containsKey(languageCode)) {
            currentLanguage = languageCode;
            usernameClient.getConsoleLogger().info(getMessage("LanguageChanged") + languageCode);
        } else {
            usernameClient.getConsoleLogger().info(getMessage("InvalidChoice"));
        }
    }

    public void displayLanguageMenu() {
        usernameClient.getConsoleLogger().info(getMessage("ChangeLanguage"));
        List<String> languageKeys = new ArrayList<>(translations.keySet());
        for (int i = 0; i < languageKeys.size(); i++) {
            usernameClient.getConsoleLogger().info((i + 1) + ". " + languageKeys.get(i));
        }
        usernameClient.getConsoleLogger().info(getMessage("ChooseOption"));
        try {
            int choice = Integer.parseInt(usernameClient.getConsoleLogger().getInput());
            if (choice >= 1 && choice <= languageKeys.size()) {
                setLanguage(languageKeys.get(choice - 1));
            }
        } catch (NumberFormatException e) {
            usernameClient.getConsoleLogger().info(getMessage("InvalidChoiceLanguage"));
        }
    }

    public String getMessage(String key) {
        return translations.getOrDefault(currentLanguage, translations.get("EN")).getOrDefault(key, key);
    }
}