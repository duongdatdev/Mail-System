package server.smtp;

import model.Mail;
import server.storage.MailStorage;

import java.io.*;
import java.net.Socket;

/** Handles SMTP connections and saves Mail objects. */
public class SmtpHandler implements Runnable {
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
                System.out.println("[SMTP] " + line);
                if (line.startsWith("HELO")) out.write("250 Hello\r\n");
                else if (line.startsWith("DATA")) {
                    out.write("354 End with . on a line\r\n");
                    inData = true;
                }
                else if (line.equals("QUIT")) {
                    out.write("221 Bye\r\n");
                    break;
                }
                else if (inData) {
                    if (line.equals(".")) {
                        Mail mail = Mail.deserialize(mailData.toString());
                        MailStorage.add(mail);
                        out.write("250 OK Stored\r\n");
                        inData = false;
                        mailData.setLength(0);
                    } else mailData.append(line).append("\n");
                } else out.write("250 OK\r\n");
                out.flush();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}

