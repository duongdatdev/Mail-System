package server.storage;

import model.Mail;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Per-user file-backed storage. Mailboxes are stored under data/mails/<sanitized-user>/
 * with files named mail-00001.eml etc. Methods are synchronized for simple thread-safety.
 */
public class MailStorage {
    private static final Path DATA_DIR = Paths.get("data", "mails");
    private static final Map<String, List<Mail>> MAILBOXES = new HashMap<>();

    static {
        try {
            Files.createDirectories(DATA_DIR);
            // load user mailboxes
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(DATA_DIR)) {
                for (Path userDir : ds) {
                    if (!Files.isDirectory(userDir)) continue;
                    String user = userDir.getFileName().toString();
                    List<Mail> list = new ArrayList<>();
                    try (DirectoryStream<Path> mails = Files.newDirectoryStream(userDir, "mail-*.eml")) {
                        List<Path> ordered = new ArrayList<>();
                        for (Path p : mails) ordered.add(p);
                        ordered.sort(Comparator.comparing(Path::toString));
                        for (Path p : ordered) {
                            try {
                                String content = new String(Files.readAllBytes(p));
                                list.add(Mail.deserialize(content));
                            } catch (IOException ioe) { ioe.printStackTrace(); }
                        }
                    }
                    MAILBOXES.put(user, list);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static String sanitize(String user) {
        return user == null ? "unknown" : user.replaceAll("[^a-zA-Z0-9@._-]", "_");
    }

    private static Path mailboxDir(String user) throws IOException {
        String s = sanitize(user);
        Path dir = DATA_DIR.resolve(s);
        Files.createDirectories(dir);
        return dir;
    }

    public static synchronized void ensureMailbox(String user) {
        String s = sanitize(user);
        MAILBOXES.computeIfAbsent(s, k -> new ArrayList<>());
        try { Files.createDirectories(DATA_DIR.resolve(s)); } catch (IOException e) { e.printStackTrace(); }
    }

    public static synchronized void add(Mail mail) {
        String to = sanitize(mail.getTo());
        List<Mail> box = MAILBOXES.computeIfAbsent(to, k -> new ArrayList<>());
        box.add(mail);
        // write to file in mailbox dir using next index
        try {
            Path dir = mailboxDir(to);
            int existing = 0;
            try (java.util.stream.Stream<Path> s = Files.list(dir)) {
                existing = (int) s.filter(p -> p.getFileName().toString().startsWith("mail-") && p.getFileName().toString().endsWith(".eml")).count();
            }
            int next = existing + 1;
            Path file = dir.resolve(String.format("mail-%05d.eml", next));
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.CREATE_NEW)) {
                w.write(mail.serialize());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Log where the mail was stored for easier debugging
        try {
            System.out.println("[MailStorage] Stored mail for '" + to + "' (subject='" + mail.getSubject() + "')");
        } catch (Exception ignored) {}
    }

    public static synchronized List<Mail> all(String user) {
        String s = sanitize(user);
        return new ArrayList<>(MAILBOXES.getOrDefault(s, Collections.emptyList()));
    }

    public static synchronized int count(String user) {
        String s = sanitize(user);
        return MAILBOXES.getOrDefault(s, Collections.emptyList()).size();
    }

    public static synchronized Mail get(String user, int id) {
        String s = sanitize(user);
        List<Mail> list = MAILBOXES.get(s);
        if (list == null || id < 1 || id > list.size()) return null;
        return list.get(id - 1);
    }
}
