package client;


import client.pop3.MailPOP3Client;
import client.smtp.MailSMTPClient;
import model.Mail;

/** High-level client wrapper combining SMTP & POP3. */
public class MailClient {
    private final String host; private final int smtpPort; private final int pop3Port;
    private final String userAddress;

    public MailClient(String host, int smtpPort, int pop3Port) {
        this(host, smtpPort, pop3Port, "user@localhost");
    }

    public MailClient(String host, int smtpPort, int pop3Port, String userAddress) {
        this.host = host; this.smtpPort = smtpPort; this.pop3Port = pop3Port;
        this.userAddress = userAddress;
    }

    public void sendMail(Mail mail) throws Exception {
        new MailSMTPClient().send(host, smtpPort, mail);
    }

    public String listAndRead(int id) throws Exception {
        return new MailPOP3Client().listAndRead(host, pop3Port, userAddress, id);
    }
}

