import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

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
        String firstname = request.getParameter("firstname");
        String lastname = request.getParameter("lastname");
        String ccnumber = request.getParameter("ccnumber");
        String expiration = request.getParameter("expiration");

        /* This example only allows firstname/password to be test/test
        /  in the real project, you should talk to the database to verify firstname/password
        */
        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {

            //CHANGE ALLLLLLLLLLLLLLLLLLLLLLLLLLLLL
            String paymentQuery = "SELECT * FROM creditcards WHERE id = ?;";
            PreparedStatement paymentStatement = conn.prepareStatement(paymentQuery);
            paymentStatement.setString(1, ccnumber);
            paymentStatement.executeQuery();
            ResultSet rs = paymentStatement.getResultSet();
            if (rs.next()) {
                // ccnumber exists, check name
                if ((firstname.equals(rs.getString("firstname"))) && (lastname.equals(rs.getString("lastname")))
                && (expiration.equals(rs.getString("expiration")))) {
                    // Login success:
                    // set this user into the session

                } else {
                    responseJsonObject.addProperty("message", "Invalid login credentials");
                }

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "Invalid login credentials");
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