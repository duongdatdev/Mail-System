package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mail implements Serializable {
    private final String from;
    private final String to;
    private final String subject;
    private final String body;
    private final LocalDateTime sentAt;
    private String signature; // Digital signature (Base64-encoded)
    private String senderPublicKey; // Sender's public key for verification (Base64-encoded)

    public Mail(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.sentAt = LocalDateTime.now();
        this.signature = null;
        this.senderPublicKey = null;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public void setSenderPublicKey(String senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
    }

    public String serialize() {
        String serialized = "FROM:" + from + "\nTO:" + to + "\nSUBJECT:" + subject + "\nTIME:" + sentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\nBODY:\n" + body + "\n";
        if (signature != null && !signature.isEmpty()) {
            serialized += "SIGNATURE:" + signature + "\n";
        }
        if (senderPublicKey != null && !senderPublicKey.isEmpty()) {
            serialized += "PUBKEY:" + senderPublicKey + "\n";
        }
        serialized += ".\n";
        return serialized;
    }

    public static Mail deserialize(String data) {
        String[] lines = data.split("\n");

        String from = "";
        String to = "";
        String subject = "";
        String signature = null;
        String senderPublicKey = null;

        StringBuilder body = new StringBuilder();
        boolean isBody = false;

        for (String line : lines) {
            if (line.equals("BODY:")) {
                isBody = true;
                continue;
            }
            if (isBody) {
                if (line.equals(".")) break;
                body.append(line).append("\n");
            } else if (line.startsWith("FROM:")) from = line.substring(5).trim();
            else if (line.startsWith("TO:")) to = line.substring(3).trim();
            else if (line.startsWith("SUBJECT:")) subject = line.substring(8).trim();
            else if (line.startsWith("SIGNATURE:")) signature = line.substring(10).trim();
            else if (line.startsWith("PUBKEY:")) senderPublicKey = line.substring(7).trim();
        }

        Mail mail = new Mail(from, to, subject, body.toString().trim());
        if (signature != null) mail.setSignature(signature);
        if (senderPublicKey != null) mail.setSenderPublicKey(senderPublicKey);
        return mail;
    }

    /** For display and POP3 retrieval. */
    public String renderRaw() {
        String raw = "From: " + from + "\r\nTo: " + to + "\r\nSubject: " + subject +
                "\r\nDate: " + sentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (signature != null && !signature.isEmpty()) {
            raw += "\r\nX-Signature: " + (signature.length() > 50 ? signature.substring(0, 50) + "..." : signature);
        }
        raw += "\r\n\r\n" + body + "\r\n";
        return raw;
    }

    @Override
    public String toString() {
        return "Mail{from='" + from + "', to='" + to + "', subject='" + subject + "', sentAt='" + sentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'}";
    }

}
