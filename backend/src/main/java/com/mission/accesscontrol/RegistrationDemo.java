package com.mission.accesscontrol;

import java.util.Scanner;

public class RegistrationDemo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        CsvUserRepository csvRepository = new CsvUserRepository();

        UserRegistrationService regService = new UserRegistrationService(csvRepository, csvRepository);


        while (true) {
            System.out.print("Do you want to register a new commander? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("n")) {
                System.out.println("Exiting Registration Tool. Data saved to CSV.");
                break;
            } else if (answer.equals("y")) {
                System.out.print("Enter new login: ");
                String newLogin = scanner.nextLine().trim();

                System.out.print("Enter new password: ");
                String newPass = scanner.nextLine();

                try {
                    regService.register(newLogin, newPass);
                    System.out.println("SUCCESS: Commander [" + newLogin + "] registered and encrypted.");
                } catch (Exception e) {
                    System.out.println("REGISTRATION ERROR: " + e.getMessage());
                }
                System.out.println();
            } else {
                System.out.println("Please type 'y' or 'n'.");
            }
        }

        scanner.close();
    }
}