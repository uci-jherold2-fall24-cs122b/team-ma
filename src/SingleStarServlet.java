import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT * from stars WHERE id = ?";


            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String name = rs.getString("name");
                String birthYear = rs.getString("birthYear") != null ? rs.getString("birthYear") : "N/A";

                String moviesQuery =
                "SELECT M.title, M.id, M.year " +
                "FROM stars AS S, stars_in_movies AS SIM, movies AS M " +
                "WHERE S.id = SIM.starId " +
                "AND SIM.movieId = M.id " +
                "AND SIM.starId = ? " +
                "ORDER BY M.year DESC, M.title ASC";

                PreparedStatement moviesStatement = conn.prepareStatement(moviesQuery);
                moviesStatement.setString(1, id);
                ResultSet moviesRs = moviesStatement.executeQuery();
                JsonArray movies = new JsonArray();

                while (moviesRs.next()) {
                    JsonObject movie = new JsonObject();
                    String title = moviesRs.getString("title");
                    String movieId = moviesRs.getString("id");
                    movie.addProperty("title",title);
                    movie.addProperty("id",movieId);
                    movies.add(movie);
                }

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_name", name);
                jsonObject.addProperty("star_dob", birthYear);
                jsonObject.add("movies", movies);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // get url of last movie list
            HttpSession session = request.getSession();
            String movieListUrl = (String) session.getAttribute("movieListUrl");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movie_list_url", movieListUrl);
            jsonArray.add(jsonObject);

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}