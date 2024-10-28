import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

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

            String paymentQuery = "SELECT * FROM creditcards WHERE id = ?;";
            PreparedStatement paymentStatement = conn.prepareStatement(paymentQuery);
            paymentStatement.setString(1, ccnumber);
            paymentStatement.executeQuery();
            ResultSet rs = paymentStatement.getResultSet();
            if (rs.next()) {
                // ccnumber exists, check name

                if ((firstname.equalsIgnoreCase(rs.getString("firstname"))) && (lastname.equalsIgnoreCase(rs.getString("lastname")))
                && (expiration.equals(rs.getString("expiration")))) {

                    String customerQuery = "SELECT c.id AS customer_id " +
                            "FROM customers c " +
                            "JOIN creditcards cc ON c.ccId = cc.id " +
                            "WHERE cc.id = ? AND cc.firstName = ? AND cc.lastName = ? AND cc.expiration = ?";
                    PreparedStatement preparedQueryStatement = conn.prepareStatement(customerQuery);
                    preparedQueryStatement.setString(1, ccnumber);
                    preparedQueryStatement.setString(2, firstname);
                    preparedQueryStatement.setString(3, lastname);
                    preparedQueryStatement.setString(4, expiration);
                    ResultSet customerRs = preparedQueryStatement.executeQuery();
                    String customerId = "";
                    if(customerRs.next()) {
                        customerId = customerRs.getString("customer_id");
                    }

                    HttpSession session = request.getSession();
                    JsonArray cartItems = (JsonArray) session.getAttribute("cartItems");

                    // Prepare your SQL statement
                    String insertQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, CURDATE())";
                    PreparedStatement preparedInsertStatement = conn.prepareStatement(insertQuery);

                    for (int i = 0; i < cartItems.size(); i++) {
                        JsonObject item = cartItems.get(i).getAsJsonObject();
                        int quantity = item.get("quantity").getAsInt();
                        String movieId = item.get("movieId").getAsString();

                        // Insert the sale for each quantity
                        for (int j = 0; j < quantity; j++) {
                            preparedInsertStatement.setString(1, customerId); // Assume customerId is defined
                            preparedInsertStatement.setString(2, movieId);
                            int updated = preparedInsertStatement.executeUpdate();
                            if (updated > 0) {
                                System.out.println("Sale added for movie ID: " + movieId);
                            }
                        }
                    }

                    session.removeAttribute("cartItems");

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                } else {
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Payment failed");
                    responseJsonObject.addProperty("message", "Invalid payment credentials. Try again.");


                }

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Payment failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "Invalid payment credentials. Try again.");
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