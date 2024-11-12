import org.xml.sax.helpers.DefaultHandler;

public class RunXMLParsers extends DefaultHandler {

    public static void main(String[] args) {
        System.out.println("Adding movies...");
        SAXParserExample movies = new SAXParserExample();
        movies.runExample();
        System.out.println("Found " + movies.duplicates + " duplicates.");

        System.out.println("Adding stars...");
        SAXParserExampleStars stars = new SAXParserExampleStars();
        stars.runExample();

        System.out.println("Updating stars in movies...");
        SAXParserExampleSIM sim = new SAXParserExampleSIM();
        sim.runExample();
        System.out.println("Found " + sim.invalid_input + " invalid inputs.");
    }
}