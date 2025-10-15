package server.storage;

import model.Mail;

import java.util.ArrayList;
import java.util.List;

public class MailStorage {
    private static final List<Mail> MAILS = new ArrayList<>();

    public static synchronized void add(Mail mail){
        MAILS.add(mail);

    }

    public static synchronized List<Mail> all() {
        return new ArrayList<>(MAILS);
    }

    public static synchronized int count() {
        return MAILS.size();
    }

    public static synchronized Mail get(int id) {
        if (id < 1 || id > MAILS.size()) return null;
        return MAILS.get(id - 1);
    }

}
