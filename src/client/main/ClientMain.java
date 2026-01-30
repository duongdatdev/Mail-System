package client.main;

import client.MailClient;
import model.Mail;

import java.util.Scanner;

/** Console UI for sending and receiving mails with signature display. */
public class ClientMain {
    public static void main(String[] args) {
        MailClient client = new MailClient("127.0.0.1", 2525, 1110);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("          🔐 Java Mail Client with Digital Signatures 🔐");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n📧 Main Menu:");
                System.out.println("  1) Send Mail (automatically signed)");
                System.out.println("  2) List/Read Mail (signature verified)");
                System.out.println("  3) Exit");
                System.out.print("\n👉 Choose option: ");
                String c = sc.nextLine();

                try {
                    switch (c) {
                        case "1":
                            System.out.print("\n📤 From: "); 
                            String from = sc.nextLine();
                            System.out.print("📥 To: "); 
                            String to = sc.nextLine();
                            System.out.print("📝 Subject: "); 
                            String sub = sc.nextLine();
                            System.out.print("💬 Body: "); 
                            String body = sc.nextLine();
                            
                            Mail mail = new Mail(from, to, sub, body);
                            client.sendMail(mail);
                            System.out.println("\n✅ Mail sent successfully");
                            System.out.println("🔏 Email was automatically signed with your private key");
                            System.out.println("🔑 Your public key was included for recipient verification");
                            break;
                            
                        case "2":
                            System.out.print("\n📬 Enter message id to retrieve (0 to just list): ");
                            int id = Integer.parseInt(sc.nextLine());
                            System.out.println("\n📩 Retrieving mail...\n");
                            String mailList = client.listAndRead(id);
                            System.out.println(mailList);
                            
                            // Parse and display signature status
                            String sigStatus = extractSignatureStatus(mailList);
                            if (!sigStatus.isEmpty()) {
                                System.out.println("═══════════════════════════════════════════════════════════════");
                                System.out.println(sigStatus);
                                System.out.println("═══════════════════════════════════════════════════════════════");
                            }
                            break;
                            
                        case "3":
                            System.out.println("\n👋 Goodbye!");
                            return;
                            
                        default:
                            System.out.println("\n❌ Unknown option: " + c);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\n❌ Invalid input: Please enter a valid number");
                } catch (Exception e) {
                    System.out.println("\n❌ Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Extract and display signature verification status.
     * Returns formatted string with signature status emoji and message.
     */
    private static String extractSignatureStatus(String emailResponse) {
        if (emailResponse.contains("X-Signature-Status:")) {
            String[] lines = emailResponse.split("\n");
            for (String line : lines) {
                if (line.contains("X-Signature-Status:")) {
                    String status = line.substring(
                        line.indexOf("X-Signature-Status:") + "X-Signature-Status:".length()
                    ).trim();
                    
                    if (status.contains("VALID")) {
                        return "✅ Digital Signature Status: VALID\n" +
                               "   ✔ Email authenticated - Confirmed from claimed sender\n" +
                               "   ✔ No tampering detected - Email content is intact";
                    } else if (status.contains("INVALID")) {
                        return "⚠️  Digital Signature Status: INVALID\n" +
                               "   ✗ Email authentication failed!\n" +
                               "   ✗ Possible tampering detected - Proceed with caution";
                    }
                }
            }
        }
        return "";
    }
}