package UI;

import java.util.Scanner;

/**
 * Created by rafid on 22/9/2017.
 */
public class LogIn {
    public String askID() {
        System.out.println("Enter your ID: ");
        Scanner scanner = new Scanner(System.in);
        System.out.println();
        return scanner.next();
    }

    public void showErrorMsg() {
        System.out.println("Already Logged in.");
    }

    public void showLoggedInMsg() {
        System.out.println("Logged in successfully.");
    }
}
