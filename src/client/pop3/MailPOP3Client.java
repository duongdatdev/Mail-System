package client.pop3;

import java.io.*;
import java.net.Socket;

/** Lists and retrieves mails from the POP3 server. */
public class MailPOP3Client {
    public void listAndRead(String host, int port, int id) throws IOException {
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("[POP3-Client] " + in.readLine());
            out.write("USER demo\r\n"); out.flush(); in.readLine();
            out.write("PASS demo\r\n"); out.flush(); in.readLine();

            out.write("LIST\r\n"); out.flush();
            String line;
            while (!(line = in.readLine()).equals(".")) System.out.println(line);

            if (id > 0) {
                out.write("RETR " + id + "\r\n"); out.flush();
                System.out.println("[POP3-Client] Message " + id + ":");
                while (!(line = in.readLine()).equals(".")) System.out.println(line);
            }
            out.write("QUIT\r\n"); out.flush();
        }
    }
}
