import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "MovieSuggestion", urlPatterns = "/api/autocomplete")
public class MovieSuggestion extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles GET requests for autocomplete suggestions.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the query parameter
        String query = request.getParameter("query");
        if (query == null || query.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Query parameter is missing or empty.");
            return;
        }
        // Process the query and return suggestions
        List<JsonObject> suggestions = null;
        try {
            suggestions = getMovieSuggestions(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JsonArray jsonArray = new JsonArray();

        for (JsonObject movie : suggestions) {
            jsonArray.add(movie);
        }

        // Set the response type to JSON and write the output
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonArray.toString());
    }

    /**
     * Queries the database for movie suggestions.
     *
     * @param query The user's input query.
     * @return A list of JsonObjects representing movies matching the query.
     */
    private List<JsonObject> getMovieSuggestions(String query) throws SQLException {
        String[] tokens = query.split("\\s+");

        // Create a list to hold the individual parts of the query
        List<String> queryParts = new ArrayList<>();

        // Add '+' and '*' to each token (word)
        for (String token : tokens) {
            queryParts.add("+" + token + "*");
        }

        // Join the tokens with a space
        String queryString = String.join(" ", queryParts);
        System.out.println(queryString);
        // SQL query
        String sql = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10;";

        List<JsonObject> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();  // Assume `dataSource` is initialized
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the queryString as the parameter for the MATCH clause
            stmt.setString(1, queryString);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String title = rs.getString("title");

                    // Create a Movie object and add it to the results
                    JsonObject movieJson = new JsonObject();
                    movieJson.addProperty("id", id);
                    movieJson.addProperty("title", title);
                    results.add(movieJson);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (JsonObject movie : results) {
            System.out.println(movie.toString());
        }

        return results;
    }
}
