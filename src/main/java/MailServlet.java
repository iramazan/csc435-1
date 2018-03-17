import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rss.MailAggregate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.stream.Collectors;

@WebServlet(name = "MailServlet")
public class MailServlet extends HttpServlet {

    private String getUser(Connection db, int id) throws SQLException {
        String getUserString =
                "SELECT user FROM users " +
                "WHERE id = ?;";
        PreparedStatement getUser = db.prepareStatement(getUserString);
        getUser.setInt(1, id);
        ResultSet result = getUser.executeQuery();
        result.next();
        return result.getString("user");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreparedStatement insertMail;
        String insertMailString =
                "INSERT INTO mail (sender, receiver, url, message) " +
                "VALUES (?, ?, ?, ?);";
        // get request parameters
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String articleUrl = jsonObject.get("url").getAsString();
        String message = jsonObject.get("message").getAsString();
        String receiver = jsonObject.get("receiver").getAsString();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            insertMail = db.prepareStatement(insertMailString);

            // get name of sender
            String sender = getUser(db, userID);

            // Add mail to database
            insertMail.setString(1, sender);
            insertMail.setString(2, receiver);
            insertMail.setString(3, articleUrl);
            insertMail.setString(4, message);
            insertMail.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        PreparedStatement getMail;
        String getMailString =
                "SELECT * FROM mail " +
                "WHERE receiver = ?;";
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            getMail = db.prepareStatement(getMailString);

            // get user name
            String user = getUser(db, userID);

            // Get mail from database
            getMail.setString(1, user);
            ResultSet result = getMail.executeQuery();

            MailAggregate mail = new MailAggregate();
            while (result.next()) {
                mail.add(result.getInt("id"),
                        result.getString("sender"), result.getString("receiver"),
                        result.getString("url"), result.getString("message"));
            }
            out.println(mail.toJson());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreparedStatement deleteMail;
        String deleteMailString =
                "DELETE FROM mail " +
                "WHERE id = ? AND receiver = ?;";
        // get request parameters
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        int mailID = jsonObject.get("id").getAsInt();
        int userID = (int) request.getAttribute("userID");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            deleteMail = db.prepareStatement(deleteMailString);

            // get user name
            String user = getUser(db, userID);

            // query delete mail
            deleteMail.setInt(1, mailID);
            deleteMail.setString(2, user);
            deleteMail.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
