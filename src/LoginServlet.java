import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // String recaptchaResponse = request.getParameter("g-recaptcha-response");
        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();
        // try {
        //     RecaptchaVerifyUtils.verify(recaptchaResponse);
        // } catch (Exception e) {
        //     responseJsonObject.addProperty("status", "fail");
        //     responseJsonObject.addProperty("message", "Captcha verification failed");
        //     response.getWriter().write(responseJsonObject.toString());
        //     response.getWriter().close();
        //     return;
        // }
        try (Connection conn = dataSource.getConnection()) {
            String loginQuery = "SELECT * FROM customers WHERE email = ?;";
            PreparedStatement loginStatement = conn.prepareStatement(loginQuery);
            loginStatement.setString(1, username);
            loginStatement.executeQuery();
            ResultSet rs = loginStatement.getResultSet();
            if(rs.next()) {
                // username exists, check password
                String encryptedPassword = rs.getString("password");
                // StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
                if (password.equals(encryptedPassword)) {
                    // Login success:
                    // set this user into the session
                    request.getSession().setAttribute("user", new User(username));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else{
                    responseJsonObject.addProperty("message", "Invalid login. Try again.");
                }

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "Invalid login. Try again.");
            }

            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }
}
