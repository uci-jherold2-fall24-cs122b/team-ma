import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;

public class SAXParserExampleSIM extends DefaultHandler {

    private String tempVal;
    private StarInMovie tempSim;
    private Connection connection;

    // List to hold StarInMovie entries for batch insert

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
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("casts124.xml", this);
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
        if (qName.equalsIgnoreCase("m")) {
            tempSim = new StarInMovie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            insertStarIntoDatabase(tempSim);

        } else if (qName.equalsIgnoreCase("a")) {
            tempSim.setName(tempVal);
        } else if (qName.equalsIgnoreCase("f")) {
            tempSim.setMovieId(tempVal);
        }
    }

    private void insertStarIntoDatabase(StarInMovie sim) {
        if (sim.getName() == null || sim.getMovieId() == null) {
            return;
        }
        // Prepare the statement to insert data into the database
        try (PreparedStatement stmt = connection.prepareStatement("CALL add_star_in_movie(?, ?)")) {

            stmt.setString(1, sim.getName());
            stmt.setString(2, sim.getMovieId());
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
