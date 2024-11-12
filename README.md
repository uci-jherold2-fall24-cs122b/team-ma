# CS122B Project
This project is a movie browsing and shopping system called Fabflix. This website allows users to search for movies by genre, title, year, director, or keyword. Users can sort the search results, browse movie listings, and add selected movies to a shopping cart. The system also includes a credit card verification process for completing purchases. Employees who work for Fabflix may also add movies/genres/stars to the database. Fabflix has also implemented XML parsing in their latest update.

## Site: [fabflix.fun](https://fabflix.fun)

## Files with Prepared Statement:
SAXParserExampleSIM.java
SAXParserExample.java
DashboardLoginServlet.java
IndexServlet.java
MovieServlet.java
PaymentServlet.java
LoginServlet.java
SingleMovieServlet.java
SingleStarServlet.java

## Parsing Optimizations
When we ran our naive approach to XML parsing, it took around 26 minutes to parse all of the files and update the database. It did not account for duplicates in the stars or in the stars_in_movies tables. It would put entries in the database with null genres, invalid movie IDs, and raise errors when attempting to add duplicates into the databases. However we implemented our optimizations and extensive error checking/logs to improve performance and properly update the database. We check all of the XML files for duplicate entries into the database, if there are null fields that should be non-null, and if the primary keys provided are invalid and unable to be inserted into the database.
An optimization time strategy that we used was the addition of BufferedInput. BufferedInput can help optimize input operations by improving the efficiency of data reading from streams. BufferedInput reduces redundant I/O calls by utilizing an internal buffer (a temporary memory storage) to hold data before it's processed. BI alleviates I/O overhead by reading large pieces of data into memory and then taking it out of memory and processing it for faster access. This allows the program to process larger amounts of data in one go, rather than small pieces gradually. This reduced our XML parsing time by 5-6 minutes, but we still needed to optimize further.
Another change we made was implementing batch processing for the larger XML files. When dealing with large datasets, especially in database operations such as inserting, updating, or deleting records, batch processing is essential for optimizing performance. Batch processing groups multiple SQL commands together and sends them to the database in one operation. This reduces the number of database round trips, improving speed and reducing load on both the client and the database. We used a batch size of 1000, as each table size (pre-parse) hovered around 5,000 - 100,000. This ensured that there were less calls to the system, but did not overload the processor with mass amounts of data. This helped bring our parse time down to 15 minutes, spending the most time on cast124.xml.

## Data Reports
(see more in logs file)


## Contributions:
Meera Jagota-
Anna Yoon- implemented reCAPTCHA, added HTTPS to AWS instance, introduced encrypted passwords, worked and troubleshot on optimizing XML parsers, registered domain name
