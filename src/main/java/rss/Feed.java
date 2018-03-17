package rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Feed {

    private String name;
    private String url;
    private List<Article> articles;

    public Feed(String url) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));

        this.name = feed.getTitle();
        this.url = url;
        this.articles = new ArrayList<>();

        for (SyndEntry entry : feed.getEntries()) {
            articles.add(new Article(entry.getTitle(), entry.getDescription().getValue(),
                    entry.getLink()));
        }
    }

    public Feed(String name, String url) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));

        this.name = name;
        this.url = url;
        this.articles = new ArrayList<>();

        for (SyndEntry entry : feed.getEntries()) {
            articles.add(new Article(entry.getTitle(), entry.getDescription().getValue(),
                    entry.getLink()));
        }
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public Iterator<Article> feedIterator() {
        return articles.iterator();
    }
}
