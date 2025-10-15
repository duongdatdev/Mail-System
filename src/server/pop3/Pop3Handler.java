package server.pop3;

import model.Mail;
import server.storage.MailStorage;

import java.io.*;
import java.net.Socket;

/** Handles POP3 requests and sends stored mails to clients. */
public class Pop3Handler implements Runnable {
    private final Socket socket;
    public Pop3Handler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            out.write("+OK Java POP3 Ready\r\n"); out.flush();
            boolean logged = false;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("USER")) out.write("+OK\r\n");
                else if (line.startsWith("PASS")) { logged = true; out.write("+OK Logged\r\n"); }
                else if (!logged) out.write("-ERR Login first\r\n");
                else if (line.equals("LIST")) {
                    out.write("+OK " + MailStorage.count() + " messages\r\n");
                    for (int i = 0; i < MailStorage.count(); i++)
                        out.write((i + 1) + " " + MailStorage.get(i + 1).renderRaw().length() + "\r\n");
                    out.write(".\r\n");
                }
                else if (line.startsWith("RETR")) {
                    int id = Integer.parseInt(line.split(" ")[1]);
                    Mail mail = MailStorage.get(id);
                    if (mail == null) out.write("-ERR No such message\r\n");
                    else {
                        out.write("+OK\r\n");
                        out.write(mail.renderRaw());
                        out.write(".\r\n");
                    }
                }
                else if (line.equals("QUIT")) { out.write("+OK Bye\r\n"); break; }
                else out.write("-ERR Unknown command\r\n");
                out.flush();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}

