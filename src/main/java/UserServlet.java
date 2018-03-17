import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.stream.Collectors;

@WebServlet(name = "UserServlet")
public class UserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        PreparedStatement newUser;
        PreparedStatement getUser;
        String newUserString =
                "INSERT INTO users (user, pass) " +
                "VALUES (?, ?);";
        String getUserString =
                "SELECT id FROM users " +
                "WHERE user=? AND pass=?;";
        String body = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        String user = jsonObject.get("user").getAsString();
        String pass = jsonObject.get("pass").getAsString();

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            newUser = db.prepareStatement(newUserString);
            getUser = db.prepareStatement(getUserString);

            // insert new user into database
            newUser.setString(1, user);
            newUser.setString(2, pass);
            newUser.executeUpdate();

            // Retrieve or create a session
            HttpSession session = request.getSession();
            synchronized(session) {
                Integer userID = (Integer) session.getAttribute("userID");
                if (userID == null) {
                    getUser.setString(1, user);
                    getUser.setString(2, pass);
                    ResultSet queryResult = getUser.executeQuery();
                    userID = -1;
                    while (queryResult.next()) {
                        userID = queryResult.getInt("id");
                    }
                }
                session.setAttribute("userID", userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Login the user if they are not already
        PrintWriter out = response.getWriter();
        PreparedStatement getUser;
        String getUserString =
                "SELECT id FROM users " +
                "WHERE user=? AND pass=?;";

        String user = request.getParameter("user");
        String pass = request.getParameter("pass");

        try (Connection db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw")) {
            getUser = db.prepareStatement(getUserString);

            // query database for user
            getUser.setString(1, user);
            getUser.setString(2, pass);
            ResultSet queryResult = getUser.executeQuery();
            Integer userID = -1;
            while (queryResult.next()) {
                userID = queryResult.getInt("id");
            }

            // Retrieve or create a session
            HttpSession session = request.getSession();
            synchronized(session) {
                session.setAttribute("userID", userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
