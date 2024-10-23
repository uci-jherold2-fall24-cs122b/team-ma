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
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


// Declaring a WebServlet called MovieServlet, which maps to url "/api/movies"
@WebServlet(name = "MovieServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // to get all from search
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String sort = request.getParameter("sort");
        String N = request.getParameter("N");
        String genreId = request.getParameter("genre_id");

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Declare our statement
            Statement statement = conn.createStatement();

            // old query : "SELECT * FROM movies as M, ratings as R WHERE M.id = R.movieId";
            // added duplicates in search...
            String query = "SELECT M.id, M.title, M.year, M.director, R.rating, R.numVotes, "
                    + "GROUP_CONCAT(GIM.genreId) AS genre_ids "
                    + "FROM movies AS M "
                    + "JOIN ratings AS R ON M.id = R.movieId "
                    + "JOIN genres_in_movies AS GIM ON M.id = GIM.movieId ";


            // take each search query and find ILIKE
            // ILIKE for non case sensitive

            if (title != null && !title.isEmpty()) {
                query += " AND m.title LIKE '%" + title + "%'";
            }
            if (year != null && !year.isEmpty()) {
                query += " AND m.year = " + year;
            }
            if (director != null && !director.isEmpty()) {
                query += " AND m.director LIKE '%" + director + "%'";
            }
            if (star != null && !star.isEmpty()) {
                query += " AND EXISTS (SELECT 1 FROM stars_in_movies sim JOIN stars s ON sim.starId = s.id WHERE sim.movieId = m.id AND s.name LIKE '%" + star + "%')";
            }
            if (genreId != null && !genreId.isEmpty()) {
                query += " AND GIM.genreId = " + genreId;
            }
            query += " GROUP BY M.id, M.title, M.year, M.director, R.rating, R.numVotes ";

            if(sort != null && !sort.isEmpty()){
                Map<String, String> sortOptions = new HashMap<>();
                sortOptions.put("0", " ORDER BY r.rating DESC, m.title ASC");
                sortOptions.put("1", " ORDER BY r.rating DESC, m.title DESC");
                sortOptions.put("2", " ORDER BY r.rating ASC, m.title ASC");
                sortOptions.put("3", " ORDER BY r.rating ASC, m.title DESC");
                sortOptions.put("4", " ORDER BY m.title ASC, r.rating ASC");
                sortOptions.put("5", " ORDER BY m.title ASC, r.rating DESC");
                sortOptions.put("6", " ORDER BY m.title DESC, r.rating ASC");
                sortOptions.put("7", " ORDER BY m.title DESC, r.rating DESC");

                String orderByClause = sortOptions.get(sort);
                if (orderByClause != null) {
                    query += " " + orderByClause;
                }
            }

            if(N != null && !N.isEmpty()) {
                query += " LIMIT " + N;
            }

                // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                int movie_year = rs.getInt("year");
                String movie_director = rs.getString("director");
                float movie_rating = rs.getFloat("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                String starsQuery = "SELECT S.name, S.id, COUNT(DISTINCT SIM_all.movieId) AS movie_count "
                        + "FROM stars AS S "
                        + "JOIN stars_in_movies AS SIM ON S.id = SIM.starId "
                        + "JOIN stars_in_movies AS SIM_all ON S.id = SIM_all.starId "
                        + "WHERE SIM.movieId = ? "
                        + "GROUP BY S.id, S.name "
                        + "ORDER BY movie_count DESC, S.name ASC "
                        + "LIMIT 3";

                PreparedStatement starsStatement = conn.prepareStatement(starsQuery);
                starsStatement.setString(1, movie_id);
                ResultSet starsRs = starsStatement.executeQuery();
                JsonArray stars = new JsonArray();

                while (starsRs.next()) {
                    String starName = starsRs.getString("name");
                    String starId = starsRs.getString("id");
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("name", starName);
                    starObject.addProperty("id", starId);

                    stars.add(starObject);
                }

                jsonObject.add("stars", stars);

                String genresQuery = "SELECT G.name FROM genres AS G "
                        + "JOIN genres_in_movies AS GIM ON G.id = GIM.genreId "
                        + "WHERE GIM.movieId = ? ORDER BY G.name ASC LIMIT 3";

                PreparedStatement genresStatement = conn.prepareStatement(genresQuery);
                genresStatement.setString(1, movie_id);
                ResultSet genresRs = genresStatement.executeQuery();
                JsonArray genres = new JsonArray();

                while (genresRs.next()) {
                    String genreName = genresRs.getString("name");
                    genres.add(genreName);
                }
                jsonObject.add("genres", genres);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            e.printStackTrace();
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}