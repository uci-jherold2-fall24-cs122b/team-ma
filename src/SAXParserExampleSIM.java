import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SAXParserExampleSIM extends DefaultHandler {

    private String tempVal;
    private StarInMovie tempSim;
    private Connection connection;
    public int invalid_input = 0;
    public int total_stars_in_movies = 0;

    // List to hold StarInMovie entries for batch insert
    private List<StarInMovie> simList = new ArrayList<>();
    private static final int BATCH_SIZE = 1000;

    public SAXParserExampleSIM() {
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
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("casts124.xml"))) {
            SAXParser sp = spf.newSAXParser();
            sp.parse(new InputSource(bufferedReader), this);   // Parse the XML document
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
        try {
            total_stars_in_movies += simList.size();
            System.out.println("Added " + total_stars_in_movies + " stars in movies");
            if (!simList.isEmpty()) {

                insertStarIntoDatabase(); // Insert any remaining entries
            }
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
        if (qName.equalsIgnoreCase("m")) {
            tempSim = new StarInMovie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            if (!simList.contains(tempSim)) {
                simList.add(tempSim);  // Add to batch list
                if (simList.size() >= BATCH_SIZE) {
                    insertStarIntoDatabase(); // Execute batch insert when the size limit is reached
                    simList.clear(); // Clear the list for next batch
                }
            } else {
                invalid_input++;  // Duplicate entry
            }
        } else if (qName.equalsIgnoreCase("a")) {
            tempSim.setName(tempVal);
        } else if (qName.equalsIgnoreCase("f")) {
            tempSim.setMovieId(tempVal);
        }
    }

    private void insertStarIntoDatabase() {
        //String checkMovieSql = "SELECT id FROM movies WHERE id = ?";
        Set<String> validMovieIds = new HashSet<>();
        String fetchMovieIdsSql = "SELECT id FROM movies";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(fetchMovieIdsSql)) {
            while (rs.next()) {
                validMovieIds.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String insertSQL = "CALL add_star_in_movie(?, ?)";

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {

            // Loop through simList only once
            for (StarInMovie sim : simList) {
                // Check if the movie exists in the database
                if (validMovieIds.contains(sim.getMovieId())) {
                    insertStmt.setString(1, sim.getName());
                    insertStmt.setString(2, sim.getMovieId());
                    insertStmt.addBatch();
                    total_stars_in_movies++;
                } else {
                    // If movie doesn't exist, log and increment invalid count
                    //System.out.println("Movie ID " + sim.getMovieId() + " does not exist, skipping entry.");
                    invalid_input++;
                }
            }

            // After checking all, execute the batch insert
            insertStmt.executeBatch();
            connection.commit();  // Commit transaction after batch insert

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();  // Rollback in case of error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }
}