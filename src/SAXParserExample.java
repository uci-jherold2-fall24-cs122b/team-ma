import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParserExample extends DefaultHandler {

    private String tempVal;
    private Movie tempMovie;
    private Connection connection;
    public Integer duplicates = 0;

    private Set<String> movieIds = new HashSet<>();

    public SAXParserExample() {
        initializeDatabase();
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
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("mains243.xml", this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            insertMovieIntoDatabase(tempMovie);

        } else if (qName.equalsIgnoreCase("fid")) {
            if (movieIds.contains(tempVal)) {
                duplicates++;
            } else {
                movieIds.add(tempMovie.getId());
                insertMovieIntoDatabase(tempMovie);
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

    private void insertMovieIntoDatabase(Movie movie) {
        if (movie.getId() == null || movie.getTitle() == null || movie.getDirector() == null) {
            return;
        }

        try (CallableStatement movieStatement = connection.prepareCall("{ CALL add_movie(?, ?, ?, ?, ?, ?, ?) }")) {
            movieStatement.setString(1, movie.getId());
            movieStatement.setString(2, movie.getTitle());
            movieStatement.setInt(3, movie.getYear());
            movieStatement.setString(4, movie.getDirector());
            // star info is null
            movieStatement.setNull(5, Types.VARCHAR);
            movieStatement.setNull(6, Types.INTEGER);
            movieStatement.setString(7, movie.getGenre());

            // Execute statement to check for duplicates
            try (ResultSet rs = movieStatement.executeQuery()) {
                if (rs.next()) {
                    String message = rs.getString("message");
                    if (message != null && message.equals("Movie already exists.")) {
                        duplicates += 1;  // Track duplicate entries
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
