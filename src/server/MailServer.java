package server;

import server.pop3.Pop3Handler;
import server.smtp.SmtpHandler;

import java.net.*;

/** Runs both SMTP and POP3 services concurrently. */
public class MailServer {
    private final int smtpPort, pop3Port;

    public MailServer(int smtpPort, int pop3Port) {
        this.smtpPort = smtpPort; this.pop3Port = pop3Port;
    }

    public void start() {
        new Thread(() -> listen(smtpPort, "SMTP"), "SMTP").start();
        new Thread(() -> listen(pop3Port, "POP3"), "POP3").start();
        System.out.println("[Server] SMTP:" + smtpPort + " POP3:" + pop3Port);
    }

    private void listen(int port, String tag) {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[" + tag + "] Listening on " + port);
            while (true) {
                Socket s = ss.accept();
                Runnable handler = tag.equals("SMTP") ? new SmtpHandler(s) : new Pop3Handler(s);
                new Thread(handler).start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}

