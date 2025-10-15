package server.main;

import server.MailServer;

public class ServerMain {
    public static void main(String[] args) {
        new MailServer(2525, 1110).start();
        System.out.println("[ServerMain] Running... Ctrl+C to stop.");
        while (true) try { Thread.sleep(60000); } catch (InterruptedException ignored) {}
    }
}

