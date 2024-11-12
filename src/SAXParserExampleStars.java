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
import java.util.List;

public class SAXParserExampleStars extends DefaultHandler {

    private String tempVal;
    private Star tempStar;
    private Connection connection;
    public int total_stars;

    private List<Star> starList = new ArrayList<>();

    public SAXParserExampleStars() {
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

        } catch ( SQLException e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("actors63.xml"))) {
            SAXParser sp = spf.newSAXParser();
            sp.parse(new InputSource(bufferedReader), this);   // Parse the XML document
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
        try {
            total_stars = starList.size();
            System.out.println("Added " + total_stars + " stars");
            if (!starList.isEmpty()) {
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
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            starList.add(tempStar);
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                tempStar.setDob(Integer.parseInt(tempVal));
            } catch (NumberFormatException e) {
                tempStar.setDob(-1);
            }
        }
    }

    private void insertStarIntoDatabase() {
        try (CallableStatement starStatement = connection.prepareCall("{ CALL add_star(?, ?, ?) }")) {
            for (Star tempStar : starList) {
                starStatement.setString(1, tempStar.getName());
                if (tempStar.getDob() == -1) {
                    starStatement.setNull(2, java.sql.Types.INTEGER);
                } else {
                    starStatement.setInt(2, tempStar.getDob());
                }
                starStatement.registerOutParameter(3, java.sql.Types.VARCHAR);

                int update = starStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}