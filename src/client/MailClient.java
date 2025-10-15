package client;


import client.pop3.MailPOP3Client;
import client.smtp.MailSMTPClient;
import model.Mail;

/** High-level client wrapper combining SMTP & POP3. */
public class MailClient {
    private final String host; private final int smtpPort; private final int pop3Port;

    public MailClient(String host, int smtpPort, int pop3Port) {
        this.host = host; this.smtpPort = smtpPort; this.pop3Port = pop3Port;
    }

    public void sendMail(Mail mail) throws Exception {
        new MailSMTPClient().send(host, smtpPort, mail);
    }

    public void listAndRead(int id) throws Exception {
        new MailPOP3Client().listAndRead(host, pop3Port, id);
    }
}

