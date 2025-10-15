package client.main;

import client.MailClient;
import model.Mail;

import java.util.Scanner;

/** Console UI for sending and receiving mails. */
public class ClientMain {
    public static void main(String[] args) {
        MailClient client = new MailClient("127.0.0.1", 2525, 1110);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Java Mail Client ===");
        while (true) {
            System.out.println("\n1) Send Mail");
            System.out.println("2) List/Read Mail");
            System.out.println("3) Exit");
            System.out.print("Choose: ");
            String c = sc.nextLine();
            try {
                switch (c) {
                    case "1":
                        System.out.print("From: "); String from = sc.nextLine();
                        System.out.print("To: "); String to = sc.nextLine();
                        System.out.print("Subject: "); String sub = sc.nextLine();
                        System.out.print("Body: "); String body = sc.nextLine();
                        client.sendMail(new Mail(from, to, sub, body));
                        break;
                    case "2":
                        System.out.print("Enter message id to RETR (0 to skip): ");
                        int id = Integer.parseInt(sc.nextLine());
                        client.listAndRead(id);
                        break;
                    case "3": return;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}

