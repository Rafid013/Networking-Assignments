package Client;

import UI.LogIn;

import java.util.Scanner;

/**
 * Created by rafid on 23/9/2017.
 */
public class ClientMain {
    public static void main(String[] args) {
        Client client = new Client();
        LogIn logIn = new LogIn();
        boolean Connected = false;
        while(!Connected) {
            String ID = logIn.askID();
            Connected = client.connect(ID);
            if (Connected) {
                logIn.showLoggedInMsg();
            }
            else logIn.showErrorMsg();
        }
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Press s to send file, r to receive file and e to exit.");
            String query = scanner.nextLine();
            if(query.compareTo("s") == 0) {
                if(!client.sendFile()) {
                    System.out.println("File sending failed.");
                }
            }
            else if(query.compareTo("r") == 0) {
                client.receiveFile();
            }
            else if (query.compareTo("e") == 0) {
                client.closeConnection();
                return;
            }
        }
    }
}
