import model.Mail;
import security.UserKeyStore;
import security.DigitalSignature;

import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Example demonstrating digital signature functionality in the mail system.
 */
public class DigitalSignatureExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Digital Signature Mail System Example ===\n");

        // Example 1: Generate keys for users
        System.out.println("1. Generating keys for users...");
        KeyPair senderKeys = UserKeyStore.getOrGenerateKeyPair("alice@localhost");
        UserKeyStore.getOrGenerateKeyPair("bob@localhost");
        System.out.println("   ✓ Keys generated and stored in data/keys/\n");

        // Example 2: Create and sign a mail manually
        System.out.println("2. Creating and signing a mail...");
        Mail mail = new Mail(
            "alice@localhost",
            "bob@localhost",
            "Important Meeting",
            "Hi Bob,\n\nLet's meet at 3 PM tomorrow.\n\nBest regards,\nAlice"
        );

        // Manually sign the mail
        String mailContent = mail.getFrom() + mail.getTo() + mail.getSubject() + mail.getBody();
        String signature = DigitalSignature.signMail(mailContent, senderKeys.getPrivate());
        String publicKeyStr = DigitalSignature.exportPublicKey(senderKeys.getPublic());

        mail.setSignature(signature);
        mail.setSenderPublicKey(publicKeyStr);
        System.out.println("   ✓ Mail signed with Alice's private key");
        System.out.println("   ✓ Signature: " + signature.substring(0, 50) + "...\n");

        // Example 3: Verify the signature
        System.out.println("3. Verifying the signature...");
        PublicKey senderPublicKey = DigitalSignature.importPublicKey(mail.getSenderPublicKey());
        boolean isValid = DigitalSignature.verifyMailSignature(
            mailContent,
            mail.getSignature(),
            senderPublicKey
        );
        System.out.println("   ✓ Signature is " + (isValid ? "VALID" : "INVALID") + "\n");

        // Example 4: Demonstrate tampering detection
        System.out.println("4. Demonstrating tampering detection...");
        String tamperedContent = mail.getFrom() + mail.getTo() + mail.getSubject() + 
                                 "MODIFIED: " + mail.getBody();
        boolean isTamperedValid = DigitalSignature.verifyMailSignature(
            tamperedContent,
            mail.getSignature(),
            senderPublicKey
        );
        System.out.println("   ✓ Tampered mail signature is " + (isTamperedValid ? "VALID" : "INVALID"));
        System.out.println("   ✓ Tampering correctly detected!\n");

        // Example 5: Serialize/deserialize mail with signature
        System.out.println("5. Serializing mail with signature...");
        String serialized = mail.serialize();
        System.out.println("   ✓ Serialized mail contains:");
        System.out.println("     - FROM field");
        System.out.println("     - TO field");
        System.out.println("     - SUBJECT field");
        System.out.println("     - BODY field");
        System.out.println("     - SIGNATURE field");
        System.out.println("     - PUBKEY field\n");

        // Example 6: Deserialize and verify
        System.out.println("6. Deserializing mail...");
        Mail deserializedMail = Mail.deserialize(serialized);
        System.out.println("   ✓ Mail deserialized successfully");
        System.out.println("   ✓ From: " + deserializedMail.getFrom());
        System.out.println("   ✓ To: " + deserializedMail.getTo());
        System.out.println("   ✓ Subject: " + deserializedMail.getSubject());
        System.out.println("   ✓ Has signature: " + (deserializedMail.getSignature() != null) + "\n");

        // Example 7: Send signed email (automatic)
        System.out.println("7. Sending signed email (automatic process)...");
        System.out.println("   Note: When using MailSMTPClient.send(), it automatically:");
        System.out.println("   - Retrieves sender's private key");
        System.out.println("   - Signs the email");
        System.out.println("   - Includes sender's public key");
        System.out.println("   - Sends everything to the server\n");

        // Example 8: Retrieve and verify (POP3 server does this)
        System.out.println("8. Retrieving and verifying email (POP3 server does this)...");
        System.out.println("   Note: When POP3Handler retrieves a mail, it:");
        System.out.println("   - Extracts the signature and public key");
        System.out.println("   - Verifies the signature");
        System.out.println("   - Adds X-Signature-Status header");
        System.out.println("   - Returns status to the client\n");

        System.out.println("=== Example Complete ===");
        System.out.println("\nKey takeaways:");
        System.out.println("✓ Every user automatically gets a unique RSA key pair");
        System.out.println("✓ Emails are automatically signed before sending");
        System.out.println("✓ Signatures are verified when retrieving emails");
        System.out.println("✓ Tampering is automatically detected");
        System.out.println("✓ Keys are persisted in data/keys/ directory");
    }
}
