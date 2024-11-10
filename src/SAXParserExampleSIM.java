import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SAXParserExampleSIM extends DefaultHandler {

    private String tempVal;
    private StarInMovie tempSim;
    private Connection connection;

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

        } catch ( SQLException e) {
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

        System.out.println(sim);
        try (CallableStatement starStatement = connection.prepareCall("{ CALL add_star_in_movie(?, ?) }")) {
            starStatement.setString(1, tempSim.getName());
            starStatement.setString(2, tempSim.getMovieId());

            int update = starStatement.executeUpdate();

            if(update > 0) {
                System.out.println("star in movie updated!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SAXParserExampleSIM spe = new SAXParserExampleSIM();
        spe.runExample();
    }

}