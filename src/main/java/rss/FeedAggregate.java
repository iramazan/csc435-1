package rss;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class FeedAggregate {

    private List<Feed> feeds;

    public FeedAggregate() {
        this.feeds = new ArrayList<>();
    }

    public void addFeed(Feed feed) {
        feeds.add(feed);
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(feeds);
    }

}
