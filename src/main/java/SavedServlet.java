import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rss.ArticleAggregate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.stream.Collectors;

@WebServlet(name = "SavedServlet")
public class SavedServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreparedStatement addArticle;
        String addArticleString =
                "INSERT INTO saved (user_id, title, url, description) " +
                "VALUES (?, ?, ?, ?);";
        // get request parameters
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String title = jsonObject.get("title").getAsString();
        String url = jsonObject.get("url").getAsString();
        String description = jsonObject.get("description").getAsString();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            addArticle = db.prepareStatement(addArticleString);

            // Add article to database
            addArticle.setInt(1, userID);
            addArticle.setString(2, title);
            addArticle.setString(3, url);
            addArticle.setString(4, description);
            addArticle.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        PreparedStatement getArticles;
        String getArticlesString =
                "SELECT title, url, description FROM saved " +
                "WHERE user_id=?;";
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            getArticles = db.prepareStatement(getArticlesString);

            // query for user's saved articles
            getArticles.setInt(1, userID);
            ResultSet queryResult = getArticles.executeQuery();

            // Display Feed for each of user's subscriptions
            ArticleAggregate articleAggregate = new ArticleAggregate();
            while (queryResult.next()) {
                articleAggregate.add(queryResult.getString("title"),
                        queryResult.getString("url"), queryResult.getString("description"));
            }

            // return json of user's feed
            out.println(articleAggregate.toJson());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreparedStatement deleteArticle;
        String deleteArticleString =
                "DELETE FROM saved " +
                "WHERE title=? AND user_id=? " +
                "LIMIT 1;";
        // get request parameters
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String title = jsonObject.get("title").getAsString();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            deleteArticle = db.prepareStatement(deleteArticleString);

            // do deletion query
            deleteArticle.setString(1, title);
            deleteArticle.setInt(2, userID);
            deleteArticle.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
