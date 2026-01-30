package security;

import java.security.*;
import java.util.Base64;

/**
 * Utility class for signing and verifying emails using RSA digital signatures.
 * Uses SHA-256 with RSA for signing mail content.
 */
public class DigitalSignature {
    private static final String ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    /**
     * Generates a new RSA key pair.
     * @return A KeyPair containing the private and public key
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * Signs mail content using a private key.
     * @param content The mail content to sign (FROM:TO:SUBJECT:BODY)
     * @param privateKey The private key
     * @return Base64-encoded signature
     */
    public static String signMail(String content, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(privateKey);
        signature.update(content.getBytes());
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    /**
     * Verifies a mail signature using the sender's public key.
     * @param content The original mail content
     * @param signatureString Base64-encoded signature
     * @param publicKey The sender's public key
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifyMailSignature(String content, String signatureString, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(content.getBytes());
            byte[] decodedSignature = Base64.getDecoder().decode(signatureString);
            return signature.verify(decodedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Exports a public key to Base64 string format for transmission.
     * @param publicKey The public key
     * @return Base64-encoded public key
     */
    public static String exportPublicKey(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    /**
     * Exports a private key to Base64 string format for storage.
     * @param privateKey The private key
     * @return Base64-encoded private key
     */
    public static String exportPrivateKey(PrivateKey privateKey) {
        byte[] privateKeyBytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(privateKeyBytes);
    }

    /**
     * Imports a public key from Base64 string.
     * @param publicKeyString Base64-encoded public key
     * @return The imported public key
     */
    public static PublicKey importPublicKey(String publicKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyString);
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Imports a private key from Base64 string.
     * @param privateKeyString Base64-encoded private key
     * @return The imported private key
     */
    public static PrivateKey importPrivateKey(String privateKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyString);
        java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }
}
