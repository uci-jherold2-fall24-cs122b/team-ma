import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParserExample extends DefaultHandler {

    private String tempVal;
    private Movie tempMovie;
    private Connection connection;
    public Integer duplicates = 0;
    public int movie_total = 0;
    public int null_genres = 0;
    private List<String> duplicateMovies = new ArrayList<>();
    private List<String> nullGenres = new ArrayList<>();
    private int genre_init;

    public Set<String> movieIds = new HashSet<>();
    private List<Movie> moviesToInsert = new ArrayList<>(); // Store movies in batches
    private static final int BATCH_SIZE = 1000;

    public SAXParserExample() {
        initializeDatabase();
        genre_init = countGenres();
    }

    public void runExample() {
        parseDocument();
        closeDatabase();
    }

    private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";
            String user = "mytestuser";
            String password = "My6$Password";
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("mains243.xml"))) {
            SAXParser sp = spf.newSAXParser();
            sp.parse(new InputSource(bufferedReader), this);   // Parse the XML document
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
        movie_total += moviesToInsert.size();
        System.out.println("Inserted " + movie_total + " movies");
        System.out.println("Null genres: " + null_genres);
        int new_genres = countGenres();
        System.out.println("New genres: " + (new_genres - genre_init));
        writeNullToFile(nullGenres);
        writeDuplicatesToFile(duplicateMovies);
        try {
            if (!moviesToInsert.isEmpty()) { // Insert any remaining record
                insertMovieIntoDatabase();
            }
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int countGenres() {
        String sql = "SELECT COUNT(*) FROM genres";
        try (PreparedStatement movieStatement = connection.prepareStatement(sql)) {
            ResultSet rs = movieStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);  // Retrieve the count from the first column
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Event Handlers for SAX Parser
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            if (!movieIds.contains(tempMovie.getId())  && tempMovie.getId() != null) {
                movieIds.add(tempMovie.getId());  // Track the movie to avoid future duplicates
                insertMovieIntoBatch(tempMovie);
            } else {
                duplicateMovies.add(tempMovie.getId());
                duplicates++;
            }
        } else if (qName.equalsIgnoreCase("fid")) {
            if (movieIds.contains(tempVal) && tempVal != null) {
                duplicateMovies.add(tempMovie.getId());
                duplicates++;
            } else {
                movieIds.add(tempMovie.getId());
            }
            tempMovie.setId(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempVal));
            } catch (NumberFormatException e) {
                return;
            }
        } else if (qName.equalsIgnoreCase("dirn")) {
            tempMovie.setDirector(tempVal);
        } else if (qName.equalsIgnoreCase("cat")) {
            tempMovie.setGenre(tempVal);
        }
    }

    private void insertMovieIntoBatch(Movie movie) {
        if (movie.getId() == null || movie.getTitle() == null || movie.getDirector() == null) {
            return;  // Skip if required fields are missing
        }

        moviesToInsert.add(movie);

        if (moviesToInsert.size() >= BATCH_SIZE) {
            insertMovieIntoDatabase(); // Execute the batch when the batch size is reached
            moviesToInsert.clear(); // Clear the list after executing the batch
        }
    }

    private void insertMovieIntoDatabase() {
        String insertSQL = "CALL add_movie(?, ?, ?, ?, ?, ?, ?) ";
        try (PreparedStatement movieStatement = connection.prepareStatement(insertSQL)) {
            for (Movie movie : moviesToInsert) {
                movieStatement.setString(1, movie.getId());
                movieStatement.setString(2, movie.getTitle());
                movieStatement.setInt(3, movie.getYear());
                movieStatement.setString(4, movie.getDirector());
                // star info is null
                movieStatement.setNull(5, Types.VARCHAR);
                movieStatement.setNull(6, Types.INTEGER);
                if (movie.getGenre() == null) {
                    nullGenres.add(movie.getId());
                    null_genres++;
                    continue;
                } else {
                    movieStatement.setString(7, movie.getGenre());
                }


                String sqlId = "SELECT * FROM movies WHERE id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sqlId)) {
                    pstmt.setString(1, movie.getId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Duplicate found, increment the duplicate counter
                            duplicateMovies.add(movie.getId());
                            duplicates += 1;
                            continue;
                        }
                    }
                }
                movieStatement.addBatch();
                movie_total++;
            }
            movieStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback(); // Roll back in case of an error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    private void writeDuplicatesToFile(List<String> list) {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdir(); // Creates the "logs" directory if it doesn't exist
        }
        try (PrintStream ps = new PrintStream(new FileOutputStream("logs/duplicate_movies.txt"))) {
            for (String movieId : list) {
                if (movieId != "null") {
                    ps.println(movieId); // Write the duplicate to the file
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeNullToFile(List<String> list) {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdir(); // Creates the "logs" directory if it doesn't exist
        }
        try (PrintStream ps = new PrintStream(new FileOutputStream("logs/null_genres.txt"))) {
            for (String movieId : list) {
                ps.println(movieId); // Write the duplicate to the file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}