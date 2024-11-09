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

@WebServlet(name = "DashboardMovieServlet", urlPatterns = "/_dashboard/api/movie")
public class DashboardMovieServlet extends HttpServlet {

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
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String birthYear = request.getParameter("birth_year");
        String genre = request.getParameter("genre");

        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            System.out.println(title);
            System.out.println(year);
            System.out.println(director);
            CallableStatement movieStatement = conn.prepareCall("{ CALL add_movie(?, ?, ?, ?, ?, ?) }");
            movieStatement.setString(1, title);
            movieStatement.setString(2, year);
            movieStatement.setString(3, director);
            movieStatement.setString(4, star);
            if (birthYear == null || birthYear.isEmpty()) {
                movieStatement.setNull(5, java.sql.Types.INTEGER);
            } else {
                movieStatement.setString(5, birthYear);
            }

            movieStatement.setString(6, genre);

            System.out.println("executing");
            ResultSet rs = movieStatement.executeQuery();
            System.out.println("complete");
            if(rs.next()){
                String message = rs.getString("message");

                if(message.equals("Movie already exists.")){
                    responseJsonObject.addProperty("status", "fail");
                }
                else{
                    responseJsonObject.addProperty("status", "success");
                }
                responseJsonObject.addProperty("message", message);
            }
            else{
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Movie could not be added. Try again.");
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