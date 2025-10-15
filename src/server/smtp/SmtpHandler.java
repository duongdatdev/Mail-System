package server.smtp;

import model.Mail;
import server.storage.MailStorage;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Handles SMTP connections and saves Mail objects. */
public class SmtpHandler implements Runnable {
    private static final Logger LOG = Logger.getLogger(SmtpHandler.class.getName());
    private final Socket socket;

    public SmtpHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            out.write("220 Simple Java SMTP Ready\r\n"); out.flush();
            String line;
            StringBuilder mailData = new StringBuilder();
            boolean inData = false;

            while ((line = in.readLine()) != null) {
                LOG.info("[SMTP] " + line);
                if (line.toUpperCase().startsWith("HELO") || line.toUpperCase().startsWith("EHLO")) {
                    out.write("250 Hello\r\n");
                } else if (line.toUpperCase().startsWith("DATA")) {
                    out.write("354 End with . on a line\r\n");
                    inData = true;
                } else if (line.equalsIgnoreCase("QUIT")) {
                    out.write("221 Bye\r\n");
                    break;
                } else if (inData) {
                    if (line.equals(".")) {
                        try {
                            Mail mail = Mail.deserialize(mailData.toString());
                            MailStorage.add(mail);
                            out.write("250 OK Stored\r\n");
                        } catch (Exception ex) {
                            LOG.log(Level.WARNING, "Failed to parse mail data", ex);
                            out.write("451 Requested action aborted: local error in processing\r\n");
                        }
                        inData = false;
                        mailData.setLength(0);
                    } else mailData.append(line).append("\n");
                } else {
                    out.write("250 OK\r\n");
                }
                out.flush();
            }
        } catch (IOException e) { LOG.log(Level.SEVERE, "SMTP handler I/O error", e); }
        finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}

