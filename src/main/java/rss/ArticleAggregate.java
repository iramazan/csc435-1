package rss;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

public class ArticleAggregate {

    List<Article> articles;

    public ArticleAggregate() {
        articles = new LinkedList<>();
    }

    public void add(String title, String url, String description) {
        articles.add(new Article(title, description, url));
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(articles);
    }

}
