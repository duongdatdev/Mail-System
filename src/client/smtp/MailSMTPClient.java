package client.smtp;

import model.Mail;

import java.io.*;
import java.net.Socket;

/** Sends a Mail object to the server using SMTP-like protocol. */
public class MailSMTPClient {
    public void send(String host, int port, Mail mail) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            System.out.println("[SMTP-Client] " + in.readLine());
            out.write("HELO localhost\r\n"); out.flush(); in.readLine();
            out.write("DATA\r\n"); out.flush(); in.readLine();

            out.write(mail.serialize());
            out.flush();
            System.out.println("[SMTP-Client] " + in.readLine());

            out.write("QUIT\r\n"); out.flush();
            System.out.println("[SMTP-Client] " + in.readLine());
        }
    }
}

