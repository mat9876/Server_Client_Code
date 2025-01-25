package Client.factory;
import Client.UsernameClient;
import Client.language.*;

public class ShowMenu {
    private final UsernameClient usernameClient;

    public LanguageManager getUsernameClientLanguageManager() {
        return usernameClient.getLanguageManager();
    }

    public CLIClientConsoleLogger getUsernameClientCLIClientConsoleLogger() {
        return usernameClient.getConsoleLogger();
    }

    public void showMenuText(String text) {
        getUsernameClientCLIClientConsoleLogger().info(getUsernameClientLanguageManager().getMessage(text));
    }
    
    public void showUserInputText(String text) {
        getUsernameClientCLIClientConsoleLogger().infoLine(getUsernameClientLanguageManager().getMessage(text));
    }
    
    public ShowMenu(UsernameClient usernameClient) {
        this.usernameClient = usernameClient;
    }

    public void showMenu() {
        getUsernameClientCLIClientConsoleLogger().info("\n" + getUsernameClientLanguageManager().getMessage("menuOptionSelection"));
        showMenuText("Option1");
        showMenuText("Option2");
        showMenuText("Option3");
        showMenuText("Option4");
        showMenuText("Option5");
        showMenuText("Option6");
        showMenuText("Option7");
        showUserInputText(("ChooseOption"));
    }

    public void showChallengeMenu() {
        showMenuText("menuChallengeSelection");
        showMenuText("OptionChallenge1");
        showMenuText("OptionChallenge2");
        showUserInputText(("ChooseOption"));
    }

    public void showRPSMenu() {
        showMenuText("menuRPSSelection");
        showMenuText("OptionRPS1");
        showMenuText("OptionRPS2");
        showMenuText("OptionRPS3");
        showUserInputText(("ChooseOption"));
    }

    public void showFileUploadMenu() {
        showMenuText("menuFileSelection");
        showMenuText("OptionFile1");
        showMenuText("OptionFile2");
        showUserInputText(("ChooseOption"));
    }
}