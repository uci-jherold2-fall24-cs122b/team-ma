import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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
            String query = "SELECT " +
                    "    m.id AS movieId, " +
                    "    m.title AS movie_title, " +
                    "    m.year AS movie_year, " +
                    "    m.director AS movie_director, " +
                    "    r.rating AS movie_rating, " +
                    "    s.id AS starId, " +
                    "    s.name AS star_name, " +
                    "    (SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) " +
                    "     FROM genres_in_movies gim " +
                    "     JOIN genres g ON gim.genreId = g.id " +
                    "     WHERE gim.movieId = m.id) AS genres " +
                    "FROM " +
                    "    movies AS m " +
                    "JOIN " +
                    "    stars_in_movies AS sim ON m.id = sim.movieId " +
                    "JOIN " +
                    "    stars AS s ON s.id = sim.starId " +
                    "LEFT JOIN " +
                    "    ratings AS r ON m.id = r.movieId " +
                    "WHERE " +
                    "    m.id = ?";  // Use placeholder for the movie ?

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("movie_title");
                int movieYear = rs.getInt("movie_year");
                String movieGenres = rs.getString("genres");
                String movieDirector = rs.getString("movie_director");
                double movieRating = rs.getDouble("movie_rating");

                // Create or update the JSON object for the movie
                if (!jsonObject.has("movie_id")) {
                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_genres", movieGenres);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_rating", movieRating);

                    JsonArray starsArray = new JsonArray();
                    JsonObject starObject = new JsonObject();

                    String starId = rs.getString("starId");
                    String starName = rs.getString("star_name");

                    // Add star object to movies array
                    starObject.addProperty("star_id", starId);
                    starObject.addProperty("star_name", starName);

                    starsArray.add(starObject);

                    // Add the movies array to the JSON object
                    jsonObject.add("stars", starsArray);
                } else {
                    JsonArray starsArray = jsonObject.getAsJsonArray("stars");

                    // Create star object
                    JsonObject starObject = new JsonObject();
                    String starId = rs.getString("starId");
                    String starName = rs.getString("star_name");

                    starObject.addProperty("star_id", starId);
                    starObject.addProperty("star_name", starName);

                    starsArray.add(starObject);
                }
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObject.toString());
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