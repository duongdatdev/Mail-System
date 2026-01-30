package security;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/**
 * Manages user key pairs for digital signatures.
 * Stores keys persistently in the file system.
 */
public class UserKeyStore {
    private static final Path KEYS_DIR = Paths.get("data", "keys");
    private static final Map<String, KeyPair> KEY_CACHE = new HashMap<>();

    static {
        try {
            Files.createDirectories(KEYS_DIR);
            loadAllKeys();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ensures a user has a key pair. If not, generates one.
     * @param user The user email
     * @return The user's KeyPair
     */
    public static synchronized KeyPair getOrGenerateKeyPair(String user) throws Exception {
        if (KEY_CACHE.containsKey(user)) {
            return KEY_CACHE.get(user);
        }

        Path publicKeyFile = KEYS_DIR.resolve(sanitize(user) + ".pub");
        Path privateKeyFile = KEYS_DIR.resolve(sanitize(user) + ".key");

        if (Files.exists(publicKeyFile) && Files.exists(privateKeyFile)) {
            // Load from file
            String publicKeyStr = new String(Files.readAllBytes(publicKeyFile));
            String privateKeyStr = new String(Files.readAllBytes(privateKeyFile));
            PublicKey pubKey = DigitalSignature.importPublicKey(publicKeyStr);
            PrivateKey privKey = DigitalSignature.importPrivateKey(privateKeyStr);
            KeyPair keyPair = new KeyPair(pubKey, privKey);
            KEY_CACHE.put(user, keyPair);
            return keyPair;
        } else {
            // Generate new key pair
            KeyPair keyPair = DigitalSignature.generateKeyPair();
            saveKeyPair(user, keyPair);
            KEY_CACHE.put(user, keyPair);
            return keyPair;
        }
    }

    /**
     * Saves a key pair to the file system.
     * @param user The user email
     * @param keyPair The key pair to save
     */
    private static void saveKeyPair(String user, KeyPair keyPair) throws IOException {
        String publicKeyStr = DigitalSignature.exportPublicKey(keyPair.getPublic());
        String privateKeyStr = DigitalSignature.exportPrivateKey(keyPair.getPrivate());

        Path publicKeyFile = KEYS_DIR.resolve(sanitize(user) + ".pub");
        Path privateKeyFile = KEYS_DIR.resolve(sanitize(user) + ".key");

        Files.write(publicKeyFile, publicKeyStr.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(privateKeyFile, privateKeyStr.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("[UserKeyStore] Generated and saved key pair for user: " + user);
    }

    /**
     * Loads all existing keys from the file system into cache.
     */
    private static void loadAllKeys() throws IOException {
        if (!Files.exists(KEYS_DIR)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(KEYS_DIR, "*.key")) {
            for (Path keyFile : stream) {
                String filename = keyFile.getFileName().toString();
                String user = filename.substring(0, filename.length() - 4); // Remove .key extension
                try {
                    String privateKeyStr = new String(Files.readAllBytes(keyFile));
                    String publicKeyStr = new String(Files.readAllBytes(KEYS_DIR.resolve(user + ".pub")));
                    PublicKey pubKey = DigitalSignature.importPublicKey(publicKeyStr);
                    PrivateKey privKey = DigitalSignature.importPrivateKey(privateKeyStr);
                    KEY_CACHE.put(user, new KeyPair(pubKey, privKey));
                } catch (Exception e) {
                    System.err.println("Failed to load keys for user: " + user);
                    e.printStackTrace();
                }
            }
        }
    }

    private static String sanitize(String user) {
        return user == null ? "unknown" : user.replaceAll("[^a-zA-Z0-9@._-]", "_");
    }

    /**
     * Gets the public key for a user (for verification by others).
     * @param user The user email
     * @return Base64-encoded public key
     */
    public static synchronized String getPublicKeyString(String user) throws Exception {
        KeyPair keyPair = getOrGenerateKeyPair(user);
        return DigitalSignature.exportPublicKey(keyPair.getPublic());
    }

    /**
     * Gets the private key for a user (for signing).
     * @param user The user email
     * @return The private key
     */
    public static synchronized PrivateKey getPrivateKey(String user) throws Exception {
        KeyPair keyPair = getOrGenerateKeyPair(user);
        return keyPair.getPrivate();
    }

    /**
     * Gets the public key for a user (for verification).
     * @param user The user email
     * @return The public key
     */
    public static synchronized PublicKey getPublicKey(String user) throws Exception {
        KeyPair keyPair = getOrGenerateKeyPair(user);
        return keyPair.getPublic();
    }
}
