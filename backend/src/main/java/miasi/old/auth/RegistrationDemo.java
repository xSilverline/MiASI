// NIE UWZGLĘDNIAMY W OFICJALNYM PROJEKCIE

package miasi.old.auth;

import org.mindrot.jbcrypt.BCrypt;
import java.util.Scanner;

public class RegistrationDemo {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);


    while (true) {
      System.out.print("Do you want to generate a new commander for JSON? (y/n): ");
      String answer = scanner.nextLine().trim().toLowerCase();

      if (answer.equals("n")) {
        System.out.println("Exiting Registration Tool.");
        break;
      } else if (answer.equals("y")) {
        System.out.print("Enter new login: ");
        String newLogin = scanner.nextLine().trim();

        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();

        try {
          String hash = BCrypt.hashpw(newPass, BCrypt.gensalt());

          System.out.println("\nSUCCESS: Commander [" + newLogin + "] encrypted.");
          System.out.println("Copy this and add to users.json:\n");

          System.out.println("  {");
          System.out.println("    \"login\": \"" + newLogin + "\",");
          System.out.println("    \"passwordHash\": \"" + hash + "\"");
          System.out.println("  }");
          System.out.println();

        } catch (Exception e) {
          System.out.println("REGISTRATION ERROR: " + e.getMessage());
        }
      } else {
        System.out.println("Please type 'y' or 'n'.");
      }
    }

    scanner.close();
  }
}