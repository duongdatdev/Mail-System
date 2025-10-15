package client.pop3;

import java.io.*;
import java.net.Socket;

/** Lists and retrieves mails from the POP3 server. */
public class MailPOP3Client {
    public String listAndRead(String host, int port, String user, int id) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            String banner = in.readLine();
            sb.append(banner).append("\n");
            out.write("USER " + user + "\r\n"); out.flush(); sb.append(in.readLine()).append("\n");
            out.write("PASS demo\r\n"); out.flush(); sb.append(in.readLine()).append("\n");

            out.write("LIST\r\n"); out.flush();
            String line;
            while (!(line = in.readLine()).equals(".")) {
                sb.append(line).append("\n");
            }

            if (id > 0) {
                out.write("RETR " + id + "\r\n"); out.flush();
                sb.append("[POP3-Client] Message ").append(id).append(":\n");
                while (!(line = in.readLine()).equals(".")) sb.append(line).append("\n");
            }
            out.write("QUIT\r\n"); out.flush();
            sb.append(in.readLine()).append("\n");
        }
        return sb.toString();
    }
}
