import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.stream.Collectors;

@WebServlet(name = "FeedServlet")
public class FeedServlet extends HttpServlet {

    // Insert Feed
    private void insertFeed(Connection db, Feed feed, int userID) throws SQLException {
        PreparedStatement addFeed;
        PreparedStatement addRelation;
        PreparedStatement getFeedID;
        String feedString =
                "INSERT IGNORE INTO feeds (url) " +
                "VALUES (?);";
        String relationString =
                "INSERT INTO users_feeds (user_id, feed_id, name) " +
                "VALUES (?, ?, ?);";
        String getIDString =
                "SELECT id FROM feeds " +
                "WHERE url=?;";

        // create sql statements
        addFeed = db.prepareStatement(feedString);
        addRelation = db.prepareStatement(relationString);
        getFeedID = db.prepareStatement(getIDString);

        // add subscription to database for user
        addFeed.setString(1, feed.getUrl());
        addFeed.executeUpdate();

        // Get id of newly created feed
        getFeedID.setString(1, feed.getUrl());
        ResultSet idResult = getFeedID.executeQuery();
        idResult.next();

        // create relationship between user and feed
        addRelation.setInt(1, userID);
        addRelation.setInt(2, idResult.getInt("id"));
        addRelation.setString(3, feed.getName());
        addRelation.executeUpdate();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get request parameters
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String feedUrl = jsonObject.get("url").getAsString();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mysql://localhost:3306/csc435?user=ian&password=qw")) {
            // create object representation of feed
            Feed feed = new Feed(null, feedUrl);
            insertFeed(db, feed, userID);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        PreparedStatement getFeeds;
        String getFeedsString =
                "SELECT user_id, url, name " +
                "FROM users u " +
                "INNER JOIN users_feeds uf ON (u.id = uf.user_id) " +
                "INNER JOIN feeds f ON (f.id = uf.feed_id) " +
                "WHERE user_id=?;";
        int userID = (int) request.getAttribute("userID");

        // Response message as HTML page
        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            // Create SQL Statements
            getFeeds = db.prepareStatement(getFeedsString);

            // query for user's feed
            getFeeds.setInt(1, userID);
            ResultSet queryResult = getFeeds.executeQuery();

            // Display Feed for each of user's subscriptions
            FeedAggregate feedAggregate = new FeedAggregate();
            while (queryResult.next()) {
                String feedName = queryResult.getString("name");
                Feed feed;
                if (feedName != null) {
                    feed = new Feed(feedName, queryResult.getString("url"));
                } else {
                    feed = new Feed(queryResult.getString("url"));
                }
                feedAggregate.addFeed(feed);
            }

            // return json of user's feed
            out.println(feedAggregate.toJson());
        } catch (SQLException | FeedException e) {
            e.printStackTrace();
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreparedStatement checkExists;
        PreparedStatement newName;
        String checkExistsString =
                "SELECT feed_id, url " +
                "FROM users u " +
                "INNER JOIN users_feeds uf ON (u.id = uf.user_id) " +
                "INNER JOIN feeds f ON (f.id = uf.feed_id) " +
                "WHERE user_id=? AND url=?;";
        String newNameString =
                "UPDATE users_feeds " +
                "SET name=? " +
                "WHERE user_id=? and feed_id=?;";
        // get request parameters
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String name = jsonObject.get("newName").getAsString();
        String url = jsonObject.get("url").getAsString();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            checkExists = db.prepareStatement(checkExistsString);
            newName = db.prepareStatement(newNameString);

            // check if the entry exists
            checkExists.setInt(1, userID);
            checkExists.setString(2, url);
            ResultSet result = checkExists.executeQuery();
            // if the entry exists modify it with new name
            if (result.next()) {
                // modify name of user's feed
                newName.setString(1, name);
                newName.setInt(2, userID);
                newName.setInt(3, result.getInt("feed_id"));
                newName.executeUpdate();
            }
            // if the entry does not exist create it
            else {
                // create object representation of feed
                Feed feed = new Feed(name, url);
                insertFeed(db, feed, userID);
                // send 201 for PUT that creates new instead of modifying
                response.setStatus(HttpServletResponse.SC_CREATED);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreparedStatement getFeedID;
        PreparedStatement rmFeed;
        String getIDString =
                "SELECT id FROM feeds " +
                "WHERE url=?;";
        String removeString =
                "DELETE FROM users_feeds " +
                "WHERE user_id=? AND feed_id=?;";
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String feedUrl = jsonObject.get("url").getAsString();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            // Create SQL Statements
            getFeedID = db.prepareStatement(getIDString);
            rmFeed = db.prepareStatement(removeString);

            // Get Feed ID
            getFeedID.setString(1, feedUrl);
            ResultSet idResult = getFeedID.executeQuery();
            idResult.next();

            // do deletion query
            rmFeed.setInt(1, userID);
            rmFeed.setInt(2, idResult.getInt("id"));
            rmFeed.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
