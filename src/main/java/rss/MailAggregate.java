package rss;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MailAggregate {

    List<Mail> mail;

    public MailAggregate() {
        mail = new ArrayList<>();
    }

    public void add(int id, String sender, String receiver, String url, String message) {
        mail.add(new Mail(id, sender, receiver, url, message));
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(mail);
    }
}
