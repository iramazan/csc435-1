import com.rometools.rome.io.FeedException;
import rss.Feed;
import rss.FeedAggregate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "ArticleServlet")
public class ArticleServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Statement statement = null;

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")){
            // query database for subscriptions
            String dbQuery = "select * from subscriptions";
            ResultSet queryResult = statement.executeQuery(dbQuery);
            FeedAggregate feed = new FeedAggregate();
            while (queryResult.next()) {
                feed.addFeed(new Feed(queryResult.getString("url")));
            }
            out.println(feed.toJson());
        } catch (SQLException | FeedException e) {
            e.printStackTrace();
        }
    }
}
