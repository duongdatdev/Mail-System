package client.smtp;

import model.Mail;
import security.DigitalSignature;
import security.UserKeyStore;

import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;

/** Sends a Mail object to the server using SMTP-like protocol. */
public class MailSMTPClient {
    public void send(String host, int port, Mail mail) throws Exception {
        // Sign the mail with sender's private key
        try {
            PrivateKey senderPrivateKey = UserKeyStore.getPrivateKey(mail.getFrom());
            String mailContent = mail.getFrom() + mail.getTo() + mail.getSubject() + mail.getBody();
            String signature = DigitalSignature.signMail(mailContent, senderPrivateKey);
            String publicKeyStr = UserKeyStore.getPublicKeyString(mail.getFrom());
            
            mail.setSignature(signature);
            mail.setSenderPublicKey(publicKeyStr);
            System.out.println("[SMTP-Client] Mail signed with digital signature");
        } catch (Exception e) {
            System.err.println("[SMTP-Client] Warning: Failed to sign mail - " + e.getMessage());
            // Continue without signature
        }

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

