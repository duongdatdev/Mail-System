package server.pop3;

import model.Mail;
import security.DigitalSignature;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Handles POP3 requests and sends stored mails to clients. */
public class Pop3Handler implements Runnable {
    private static final Logger LOG = Logger.getLogger(Pop3Handler.class.getName());
    private final Socket socket;

    public Pop3Handler(Socket socket) { this.socket = socket; }

    /**
     * Verifies the digital signature of a mail.
     * @param mail The mail to verify
     * @return true if signature is valid or not present, false if invalid
     */
    private boolean verifyMailSignature(Mail mail) {
        if (mail.getSignature() == null || mail.getSenderPublicKey() == null) {
            return true; // No signature to verify
        }

        try {
            String mailContent = mail.getFrom() + mail.getTo() + mail.getSubject() + mail.getBody();
            PublicKey publicKey = DigitalSignature.importPublicKey(mail.getSenderPublicKey());
            boolean isValid = DigitalSignature.verifyMailSignature(mailContent, mail.getSignature(), publicKey);
            
            if (isValid) {
                LOG.info("[POP3] Digital signature verified for mail from: " + mail.getFrom());
            } else {
                LOG.warning("[POP3] Digital signature INVALID for mail from: " + mail.getFrom());
            }
            return isValid;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "[POP3] Failed to verify signature: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            out.write("+OK Java POP3 Ready\r\n"); out.flush();
            boolean logged = false;
            String currentUser = null;
            String line;
            while ((line = in.readLine()) != null) {
                LOG.info("[POP3] " + line);
                String up = line.toUpperCase();
                if (up.startsWith("USER")) {
                    String[] parts = line.split(" ", 2);
                    currentUser = parts.length > 1 ? parts[1].trim() : "";
                    server.storage.MailStorage.ensureMailbox(currentUser);
                    out.write("+OK\r\n");
                } else if (up.startsWith("PASS")) {
                    // In this simple implementation, any PASS is accepted after USER.
                    logged = true; out.write("+OK Logged\r\n");
                } else if (!logged) {
                    out.write("-ERR Login first\r\n");
                } else if (up.equals("LIST")) {
                    out.write("+OK " + server.storage.MailStorage.count(currentUser) + " messages\r\n");
                    for (int i = 0; i < server.storage.MailStorage.count(currentUser); i++) {
                        Mail m = server.storage.MailStorage.get(currentUser, i + 1);
                        int len = m == null ? 0 : m.renderRaw().length();
                        out.write((i + 1) + " " + len + "\r\n");
                    }
                    out.write(".\r\n");
                } else if (up.startsWith("RETR")) {
                    String[] parts = line.split(" ");
                    if (parts.length < 2) { out.write("-ERR Missing message number\r\n"); }
                    else {
                        try {
                            int id = Integer.parseInt(parts[1].trim());
                            Mail mail = server.storage.MailStorage.get(currentUser, id);
                            if (mail == null) out.write("-ERR No such message\r\n");
                            else {
                                boolean signatureValid = verifyMailSignature(mail);
                                out.write("+OK\r\n");
                                if (mail.getSignature() != null && !mail.getSignature().isEmpty()) {
                                    out.write("X-Signature-Status: " + (signatureValid ? "VALID" : "INVALID") + "\r\n");
                                }
                                out.write(mail.renderRaw());
                                out.write(".\r\n");
                            }
                        } catch (NumberFormatException nfe) { out.write("-ERR Invalid message number\r\n"); }
                    }
                } else if (up.equals("QUIT")) {
                    out.write("+OK Bye\r\n"); break;
                } else {
                    out.write("-ERR Unknown command\r\n");
                }
                out.flush();
            }
        } catch (IOException e) { LOG.log(Level.SEVERE, "POP3 handler I/O error", e); }
        finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}

