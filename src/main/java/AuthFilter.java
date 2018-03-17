import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@WebFilter(filterName = "AuthFilter")
public class AuthFilter implements Filter {

    private FilterConfig filterConfig = null;
    private Connection db;
    private Statement statement;

    public void destroy() {
        this.filterConfig = null;
        try {
            if (this.statement != null) {
                this.statement.close();
            }
            if (this.db != null) {
                this.db.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // get the user's session
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(407, "Must login to view feed.");
        }
        Integer userID;
        synchronized (session) {
            userID = (Integer) session.getAttribute("userID");
        }
        req.setAttribute("userID", userID);

        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = null;
        try {
            // Create database connection
            this.db = DriverManager.getConnection("jdbc:mariadb://localhost:3306/csc435?user=ian&password=qw");
            this.statement = db.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
