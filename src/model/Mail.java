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

    public Mail(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.sentAt = LocalDateTime.now();
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

    public String serialize() {
        return "FROM:" + from + "\nTO:" + to + "\nSUBJECT:" + subject + "\nTIME:" + sentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\nBODY:\n" + body + "\n.\n";
    }

    public static Mail deserialize(String data) {
        String[] lines = data.split("\n");

        String from = "";
        String to = "";
        String subject = "";

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
        }

        return new Mail(from,to,subject,body.toString().trim());
    }

    /** For display and POP3 retrieval. */
    public String renderRaw() {
        return "From: " + from + "\r\nTo: " + to + "\r\nSubject: " + subject +
                "\r\nDate: " + sentAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                "\r\n\r\n" + body + "\r\n";
    }

}
