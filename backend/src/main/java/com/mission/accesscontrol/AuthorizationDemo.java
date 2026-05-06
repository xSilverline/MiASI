package com.mission.accesscontrol;

import java.util.Scanner;

public class AuthorizationDemo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Access control system");


        while (true) {
            System.out.print("Do you want to register a new commander? (yes/no): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("no")) {
                break;
            } else if (answer.equals("yes")) {
                System.out.print("Enter new login: ");
                String newLogin = scanner.nextLine();
                System.out.print("Enter new password: ");
                String newPass = scanner.nextLine();

                UserRegistration.createNewCommander(newLogin, newPass);
            } else {
                System.out.println("Please type 'yes' or 'no'.");
            }
        }

        Authorization authModule = new Authorization();
        String currentToken = null;

        while (true) {
            System.out.println("Available actions: login, logout, exit");
            System.out.print("Action > ");
            String action = scanner.nextLine().toLowerCase().trim();

            try {
                switch (action) {
                    case "login":
                        System.out.print("Username: ");
                        String user = scanner.nextLine();
                        System.out.print("Password: ");
                        String pass = scanner.nextLine();

                        currentToken = authModule.login(user, pass);
                        System.out.println("SUCCESS: Session started. Token: " + currentToken);
                        break;

                    case "logout":
                        if (currentToken == null) {
                            System.out.println("ERROR: No active session to log out.");
                        } else {
                            authModule.logout(currentToken);
                            currentToken = null;
                            System.out.println("SUCCESS: Logged out successfully.");
                        }
                        break;

                    case "exit":
                        System.out.println("Shutting down Mission Control.");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Unknown command.");
                }
            } catch (Exception e) {

                System.out.println("ACCESS DENIED: " + e.getMessage());
            }
        }
    }
}