console.log("HIII!");
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href=single-movie.html?id=' + resultData[i]['movie_id'] + '>'
            + resultData[i]["movie_title"] + '</a>' +
            "</th>";

        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        rowHTML += "<th>";
        let genres = resultData[i]["genres"];
        for (let j = 0; j < genres.length; j++) {
            rowHTML += genres[j];
            if (j < genres.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";


        rowHTML += "<th>";
        let stars = resultData[i]["stars"];
        for (let j = 0; j < stars.length; j++) {
            // Add a link to single-star.html with id passed with GET url parameter
            rowHTML +=
                '<a href=single-star.html?id=' + stars[j]['id'] + '>'
                + stars[j]["name"] +     // display star_name for the link text
                '</a>';
            if (j < stars.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
//
//
//
// /**
//  * Submit form content with GET method
//  * @param searchEvent
//  */
//function handleSearchInfo(searchEvent) {
//    console.log("submit search form");
//    console.log(searchEvent);
//    let formData = jQuery("#search").serialize();
    // Redirect to movie.html with the search query as a URL parameter
//     $.ajax("api/movies", {
//         method: "GET",
//         data: search.serialize(),
//         success: resultDataString => {
//             let resultDataJson = JSON.parse(resultDataString);
//         }
//     });
//
//     // clear input form
//     search[0].reset();
// }
//
//
function handleSearchInfo(searchEvent) {
    console.log("submit search form");

    let formData = $("#search").serialize();
    console.log("Serialized form data: ", formData);

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies", // Setting request URL, which is mapped by MoviesServlet in MoviesServlet.java
        data: formData, // Send the search form data with the request
        success: (resultData) => handleMovieResult(resultData), // Setting callback function to handle data returned successfully
        error: (jqXHR, textStatus, errorThrown) => {
            // Log the error to the console
            console.error("AJAX error: ", textStatus, errorThrown);
            console.error("Response: ", jqXHR.responseText);
        }
    });
}

$("#search").submit(handleSearchInfo);

// Extract query parameters
const params = new URLSearchParams(window.location.search);
const title = params.get("title");
const year = params.get("year");
const director = params.get("director");
const star = params.get("star");
const genre_id = params.get("genre_id");

//
// /**
//  * Once this .js is loaded, following scripts will be executed by the browser
//  */
//
// // Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    data: { title, year, director, star, genre_id }, // Send parameters to the server
    success: handleMovieResult,
    error: (jqXHR, textStatus, errorThrown) => {
        console.error("AJAX error: ", textStatus, errorThrown);
    }
});

/*jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MovieServlet
});*/
//
//
// search.submit(handleSearchInfo);
