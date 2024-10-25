import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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
            String query = "SELECT * from movies, ratings WHERE id = movieId AND id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", title);
                jsonObject.addProperty("movie_year", year);
                jsonObject.addProperty("movie_director", director);
                jsonObject.addProperty("movie_rating", rating);

                String starsQuery = "SELECT S.name, S.id, COUNT(DISTINCT SIM_all.movieId) AS movie_count "
                        + "FROM stars AS S "
                        + "JOIN stars_in_movies AS SIM ON S.id = SIM.starId "
                        + "JOIN stars_in_movies AS SIM_all ON S.id = SIM_all.starId "
                        + "WHERE SIM.movieId = ?"
                        + "GROUP BY S.id, S.name "
                        + "ORDER BY movie_count DESC, S.name ASC";

                PreparedStatement starsStatement = conn.prepareStatement(starsQuery);
                starsStatement.setString(1, id);
                ResultSet starsRs = starsStatement.executeQuery();
                JsonArray stars = new JsonArray();

                while (starsRs.next()) {
                    JsonObject star = new JsonObject();
                    String starName = starsRs.getString("name");
                    String starId = starsRs.getString("id");
                    star.addProperty("name",starName);
                    star.addProperty("id", starId);
                    stars.add(star);
                }
                jsonObject.add("stars", stars);

                String genresQuery = "SELECT G.name, G.id FROM genres AS G "
                        + "JOIN genres_in_movies AS GIM ON G.id = GIM.genreId "
                        + "WHERE GIM.movieId = ? ORDER BY G.name ASC";

                PreparedStatement genresStatement = conn.prepareStatement(genresQuery);
                genresStatement.setString(1, id);
                ResultSet genresRs = genresStatement.executeQuery();
                JsonArray genres = new JsonArray();

                while (genresRs.next()) {
                    String genreName = genresRs.getString("name");
                    genres.add(genreName);
                }

                jsonObject.add("movie_genres", genres);

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