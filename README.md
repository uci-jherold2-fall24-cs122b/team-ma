# CS122B Project
This project is a movie browsing and shopping system called Fabflix. This website allows users to search for movies by genre, title, year, director, or keyword. Users can sort the search results, browse movie listings, and add selected movies to a shopping cart. The system also includes a credit card verification process for completing purchases. Employees who work for Fabflix may also add movies/genres/stars to the database. Fabflix has also implemented XML parsing in their latest update. Fabflix also has implemented full text search.

## Site: [fabflix.fun](https://fabflix.fun)

## Files with Prepared Statement:
- SAXParserExampleSIM.java
- SAXParserExample.java
- DashboardLoginServlet.java
- IndexServlet.java
- MovieServlet.java
- PaymentServlet.java
- LoginServlet.java
- SingleMovieServlet.java
- SingleStarServlet.java

## Files with Prepared Statement and User Input (JDBC Pooling)
- DashboardLoginServlet.java
- IndexServlet.java
- MovieServlet.java
- PaymentServlet.java
- LoginServlet.java
- SingleMovieServlet.java
- SingleStarServlet.java
- context.xml

# Connection Pooling
- #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
- 000-default.conf
    
- #### Explain how Connection Pooling is utilized in the Fabflix code.
- In our Fabflix website, connection pooling is used to manage database connections efficiently. Connection pooling is a technique that allows database connections to be reused, rather than new connections each time the application interacts with the database. This greatly improves performance and scalability. In context.xml, a resource is defined which represents a connection pool for the MySQL database. In the servlets above, the connection pool is accessed using the JNDI lookup mechanism.

- #### Explain how Connection Pooling works with two backend SQL.
- Connection pooling is also useful in working with two backend SQL databases as it reduces the overhead of opening and closing connections most of the time. This can be addressed by creating distinct DataSource objects for each database so that their connections are initiated and maintained separately, therefore enhancing scalability and performance. Nonetheless, operating multiple databases increases complexity, particularly when addressing transactions spanning more than one database. Multiple databaser’s connection pooling can be configured by creating separate DataSource resources, with specific connection pool for each, and linked to JNDI in your application.



## Contributions:
Meera Jagota- finished tasks 3 and 4. Implemented master-slave replication, connection pooling, and sticky sessions 

Anna Yoon- autocomplete search and JDBC pooling
