/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");
    starInfoElement.empty();

    console.log("Result Data: ", resultData);


    // append two html <p> created to the h3 body, which will refresh the page
    //starInfoElement.append("<p>Star Name: " + resultData[0]["star_name"] + "</p>");

    if (resultData[0]) {
        starInfoElement.append("<h2>" + resultData[0]["star_name"] + "</h2>");
        console.log("Star DOB: ", resultData[0]["star_dob"]);

        const starDob = (resultData[0]["star_dob"] !== null) ? resultData[0]["star_dob"] : "N/A";
        starInfoElement.append("<p>Date of Birth: " + starDob + "</p>");
    } else {
        starInfoElement.append("<p>Star not found.</p>");
        return; // Exit if star info is not found
    }
    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");
    starTableBodyElement.empty();

    // Concatenate the html tags with resultData jsonObject to create table rows
    let movies = resultData[0]["movies"];

    if (movies && Array.isArray(movies)) {
        for (let i = 0; i < movies.length; i++) {
            let rowHTML = "<tr>";

            let movieTitle = movies[i]["title"];
            let movieId = movies[i]["id"];

            rowHTML += "<th><a href='single-movie.html?id=" + movieId + "'>" + movieTitle + "</a></th>";
            rowHTML += "</tr>";

            // Append the row created to the table body
            starTableBodyElement.append(rowHTML);
        }
    } else {
        starTableBodyElement.append("<tr><td colspan='3'>No movies found.</td></tr>"); // Fallback message for no movies
    }

    const movieListUrl = document.getElementById('movie-list-url');
    movieListUrl.href = "movie.html?" + resultData[1]["movie_list_url"];

    // Append the row created to the table body, which will refresh the page
    //starTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});