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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardStarServlet", urlPatterns = "/_dashboard/api/star")
public class DashboardStarServlet extends HttpServlet {

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
        String starName = request.getParameter("starname");
        String birthYear = request.getParameter("birthyear");

        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {

            CallableStatement starStatement = conn.prepareCall("{ CALL add_star(?, ?, ?) }");
            starStatement.setString(1, starName);
            if (birthYear == null || birthYear.isEmpty()) {
                starStatement.setNull(2, java.sql.Types.INTEGER);
            } else {
                starStatement.setString(2, birthYear);
            }
            starStatement.registerOutParameter(3, java.sql.Types.VARCHAR);
            System.out.println("HI");
            int update = starStatement.executeUpdate();

            if(update > 0){
                String newStarId = starStatement.getString(3);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Success! Star added with ID: " + newStarId);
            }
            else{
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Star could not be added. Try again.");
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