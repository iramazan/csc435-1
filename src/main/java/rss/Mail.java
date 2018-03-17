package rss;

public class Mail {
    private int id;
    private String sender;
    private String receiver;
    private String url;
    private String message;

    public Mail(int id, String sender, String receiver, String url, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.url = url;
        this.message = message;
    }

}
