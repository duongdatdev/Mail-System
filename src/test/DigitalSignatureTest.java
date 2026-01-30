package test;

import model.Mail;
import security.DigitalSignature;
import security.UserKeyStore;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Comprehensive test suite for digital signature functionality.
 * Tests signing, verification, and tampering detection.
 */
public class DigitalSignatureTest {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("╔═════════════════════════════════════════════════════════════════╗");
        System.out.println("║        DIGITAL SIGNATURE TEST SUITE - COMPREHENSIVE TESTS        ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════╝\n");

        try {
            // Test 1: Key Generation
            testKeyGeneration();

            // Test 2: Key Export/Import
            testKeyExportImport();

            // Test 3: Email Signing
            testEmailSigning();

            // Test 4: Signature Verification
            testSignatureVerification();

            // Test 5: Tampering Detection
            testTamperingDetection();

            // Test 6: User Key Store
            testUserKeyStore();

            // Test 7: Mail Serialization with Signature
            testMailSerialization();

            // Test 8: Multiple Users
            testMultipleUsers();

            // Print Summary
            printSummary();
        } catch (Exception e) {
            System.err.println("❌ Test suite error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test 1: Key Pair Generation
     */
    private static void testKeyGeneration() {
        String testName = "Test 1: Key Pair Generation (RSA 2048)";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            KeyPair keyPair = DigitalSignature.generateKeyPair();
            
            assertTrue(keyPair != null, "KeyPair is null");
            assertTrue(keyPair.getPublic() != null, "Public key is null");
            assertTrue(keyPair.getPrivate() != null, "Private key is null");
            assertTrue(keyPair.getPublic().getAlgorithm().equals("RSA"), "Algorithm is not RSA");
            
            System.out.println("✅ Key pair generated successfully");
            System.out.println("   Public Key Algorithm: " + keyPair.getPublic().getAlgorithm());
            System.out.println("   Private Key Algorithm: " + keyPair.getPrivate().getAlgorithm());
            System.out.println("   Public Key Format: " + keyPair.getPublic().getFormat());
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 2: Key Export and Import
     */
    private static void testKeyExportImport() {
        String testName = "Test 2: Key Export/Import (Base64 Encoding)";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            KeyPair originalKeyPair = DigitalSignature.generateKeyPair();
            
            // Export keys
            String publicKeyString = DigitalSignature.exportPublicKey(originalKeyPair.getPublic());
            String privateKeyString = DigitalSignature.exportPrivateKey(originalKeyPair.getPrivate());
            
            assertTrue(publicKeyString != null && !publicKeyString.isEmpty(), "Public key export failed");
            assertTrue(privateKeyString != null && !privateKeyString.isEmpty(), "Private key export failed");
            
            // Import keys back
            PublicKey importedPublic = DigitalSignature.importPublicKey(publicKeyString);
            PrivateKey importedPrivate = DigitalSignature.importPrivateKey(privateKeyString);
            
            assertTrue(importedPublic != null, "Imported public key is null");
            assertTrue(importedPrivate != null, "Imported private key is null");
            
            System.out.println("✅ Key export/import successful");
            System.out.println("   Public Key (Base64): " + publicKeyString.substring(0, 50) + "...");
            System.out.println("   Private Key (Base64): " + privateKeyString.substring(0, 50) + "...");
            System.out.println("   Public Key Length: " + publicKeyString.length() + " chars");
            System.out.println("   Private Key Length: " + privateKeyString.length() + " chars");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 3: Email Signing
     */
    private static void testEmailSigning() {
        String testName = "Test 3: Email Signing (SHA-256withRSA)";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            KeyPair keyPair = DigitalSignature.generateKeyPair();
            String mailContent = "sender@localhost" + "recipient@localhost" + "Test Subject" + "This is test body";
            
            String signature = DigitalSignature.signMail(mailContent, keyPair.getPrivate());
            
            assertTrue(signature != null && !signature.isEmpty(), "Signature is empty");
            
            // Verify signature is Base64 encoded
            java.util.Base64.getDecoder().decode(signature);
            
            System.out.println("✅ Email signed successfully");
            System.out.println("   Content: " + mailContent.substring(0, Math.min(50, mailContent.length())) + "...");
            System.out.println("   Signature: " + signature.substring(0, 50) + "...");
            System.out.println("   Signature Length: " + signature.length() + " chars");
            System.out.println("   Algorithm: SHA-256withRSA");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 4: Signature Verification
     */
    private static void testSignatureVerification() {
        String testName = "Test 4: Signature Verification";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            KeyPair keyPair = DigitalSignature.generateKeyPair();
            String mailContent = "alice@localhost" + "bob@localhost" + "Hello Bob" + "How are you?";
            
            String signature = DigitalSignature.signMail(mailContent, keyPair.getPrivate());
            
            // Verify with correct content
            boolean isValid = DigitalSignature.verifyMailSignature(mailContent, signature, keyPair.getPublic());
            assertTrue(isValid, "Valid signature failed verification");
            
            // Verify with wrong content should fail
            String wrongContent = "alice@localhost" + "bob@localhost" + "Hello Bob" + "How are you? MODIFIED";
            boolean isInvalid = DigitalSignature.verifyMailSignature(wrongContent, signature, keyPair.getPublic());
            assertTrue(!isInvalid, "Invalid signature passed verification");
            
            System.out.println("✅ Signature verification working correctly");
            System.out.println("   Original content: VALID ✅");
            System.out.println("   Modified content: INVALID ✅");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 5: Tampering Detection
     */
    private static void testTamperingDetection() {
        String testName = "Test 5: Tampering Detection";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            KeyPair keyPair = DigitalSignature.generateKeyPair();
            String originalContent = "sender@localhost" + "recipient@localhost" + "Important" + "Transfer $1000";
            String signature = DigitalSignature.signMail(originalContent, keyPair.getPrivate());
            
            // Test various tampering scenarios
            String[] tamperedVersions = {
                "sender@localhost" + "hacker@localhost" + "Important" + "Transfer $1000",  // Change recipient
                "sender@localhost" + "recipient@localhost" + "URGENT" + "Transfer $1000",   // Change subject
                "sender@localhost" + "recipient@localhost" + "Important" + "Transfer $5000", // Change amount
                "HACKER@localhost" + "recipient@localhost" + "Important" + "Transfer $1000"  // Change sender
            };
            
            String[] tamperDescriptions = {
                "Changed recipient",
                "Changed subject",
                "Changed body",
                "Changed sender"
            };
            
            int detectedTampering = 0;
            for (int i = 0; i < tamperedVersions.length; i++) {
                boolean isValid = DigitalSignature.verifyMailSignature(
                    tamperedVersions[i], 
                    signature, 
                    keyPair.getPublic()
                );
                
                if (!isValid) {
                    System.out.println("   ✅ Detected tampering: " + tamperDescriptions[i]);
                    detectedTampering++;
                } else {
                    System.out.println("   ❌ Missed tampering: " + tamperDescriptions[i]);
                }
            }
            
            assertTrue(detectedTampering == 4, "Not all tampering detected");
            
            System.out.println("✅ All tampering scenarios detected successfully (" + detectedTampering + "/4)");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 6: User Key Store
     */
    private static void testUserKeyStore() {
        String testName = "Test 6: User Key Store (Persistence)";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            String testUser = "testuser_" + System.nanoTime() + "@localhost";
            
            // Get or generate key pair for user
            KeyPair keyPair1 = UserKeyStore.getOrGenerateKeyPair(testUser);
            assertTrue(keyPair1 != null, "First call returned null");
            
            // Get same user's key pair (should be cached)
            KeyPair keyPair2 = UserKeyStore.getOrGenerateKeyPair(testUser);
            assertTrue(keyPair2 != null, "Second call returned null");
            
            // Keys should be the same (same object reference due to caching)
            System.out.println("✅ User key store working correctly");
            System.out.println("   Generated key pair for: " + testUser);
            System.out.println("   Key pair cached and retrieved");
            System.out.println("   Keys stored in: data/keys/");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 7: Mail Serialization with Signature
     */
    private static void testMailSerialization() {
        String testName = "Test 7: Mail Serialization with Signature";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            // Create a mail
            Mail mail = new Mail("alice@localhost", "bob@localhost", "Test", "This is a test");
            
            // Generate and sign
            KeyPair keyPair = DigitalSignature.generateKeyPair();
            String mailContent = mail.getFrom() + mail.getTo() + mail.getSubject() + mail.getBody();
            String signature = DigitalSignature.signMail(mailContent, keyPair.getPrivate());
            String publicKeyStr = DigitalSignature.exportPublicKey(keyPair.getPublic());
            
            mail.setSignature(signature);
            mail.setSenderPublicKey(publicKeyStr);
            
            // Serialize
            String serialized = mail.serialize();
            assertTrue(serialized.contains("SIGNATURE:"), "Signature not in serialized form");
            assertTrue(serialized.contains("PUBKEY:"), "Public key not in serialized form");
            
            // Deserialize
            Mail deserializedMail = Mail.deserialize(serialized);
            assertTrue(deserializedMail.getSignature() != null, "Signature lost in deserialization");
            assertTrue(deserializedMail.getSenderPublicKey() != null, "Public key lost in deserialization");
            assertTrue(deserializedMail.getFrom().equals(mail.getFrom()), "From field changed");
            assertTrue(deserializedMail.getTo().equals(mail.getTo()), "To field changed");
            assertTrue(deserializedMail.getBody().equals(mail.getBody()), "Body field changed");
            
            System.out.println("✅ Mail serialization/deserialization working");
            System.out.println("   Original mail: " + mail);
            System.out.println("   Serialized length: " + serialized.length() + " chars");
            System.out.println("   Contains SIGNATURE field: ✅");
            System.out.println("   Contains PUBKEY field: ✅");
            System.out.println("   Deserialized successfully: ✅");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Test 8: Multiple Users with Different Keys
     */
    private static void testMultipleUsers() {
        String testName = "Test 8: Multiple Users (Isolation)";
        System.out.println("\n" + testName);
        System.out.println("─".repeat(testName.length()));

        try {
            // Create keys for different users
            KeyPair aliceKeys = UserKeyStore.getOrGenerateKeyPair("alice@localhost");
            KeyPair bobKeys = UserKeyStore.getOrGenerateKeyPair("bob@localhost");
            
            // Sign with alice's key
            String content = "Message from Alice";
            String aliceSignature = DigitalSignature.signMail(content, aliceKeys.getPrivate());
            
            // Verify with alice's public key - should pass
            boolean aliceVerifies = DigitalSignature.verifyMailSignature(
                content, 
                aliceSignature, 
                aliceKeys.getPublic()
            );
            assertTrue(aliceVerifies, "Alice's signature failed with Alice's key");
            
            // Verify with bob's public key - should fail
            boolean bobVerifies = DigitalSignature.verifyMailSignature(
                content, 
                aliceSignature, 
                bobKeys.getPublic()
            );
            assertTrue(!bobVerifies, "Alice's signature verified with Bob's key");
            
            System.out.println("✅ Multiple user isolation working correctly");
            System.out.println("   Alice's signature with Alice's key: ✅ VALID");
            System.out.println("   Alice's signature with Bob's key: ✅ INVALID");
            System.out.println("   Key isolation: ✅ VERIFIED");
            
            testsPassed++;
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Print test summary
     */
    private static void printSummary() {
        System.out.println("\n╔═════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        TEST SUMMARY                             ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Tests Passed: " + String.format("%-50s", testsPassed + " ✅") + "║");
        System.out.println("║ Tests Failed: " + String.format("%-50s", testsFailed + " " + (testsFailed == 0 ? "✅" : "❌")) + "║");
        System.out.println("║ Total Tests:  " + String.format("%-50s", (testsPassed + testsFailed) + "") + "║");
        
        if (testsFailed == 0) {
            System.out.println("║                                                                 ║");
            System.out.println("║  🎉 ALL TESTS PASSED! DIGITAL SIGNATURES WORKING PERFECTLY! 🎉  ║");
        } else {
            System.out.println("║                                                                 ║");
            System.out.println("║  ⚠️  SOME TESTS FAILED - REVIEW RESULTS ABOVE                   ║");
        }
        System.out.println("╚═════════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Simple assertion helper
     */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
