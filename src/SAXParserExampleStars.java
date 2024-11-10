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

public class SAXParserExampleStars extends DefaultHandler {

    private String tempVal;
    private Star tempStar;
    private Connection connection;

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

        } catch ( SQLException e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("actors63.xml", this);
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
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            insertStarIntoDatabase(tempStar);
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

    private void insertStarIntoDatabase(Star star) {

        System.out.println(star);
        try (CallableStatement starStatement = connection.prepareCall("{ CALL add_star(?, ?, ?) }")) {
            starStatement.setString(1, tempStar.getName());
            if (tempStar.getDob() == -1) {
                starStatement.setNull(2, java.sql.Types.INTEGER);
            } else {
                starStatement.setInt(2, tempStar.getDob());
            }
            starStatement.registerOutParameter(3, java.sql.Types.VARCHAR);

            int update = starStatement.executeUpdate();

            if(update > 0) {
                System.out.println("star added!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SAXParserExampleStars spe = new SAXParserExampleStars();
        spe.runExample();
    }

}