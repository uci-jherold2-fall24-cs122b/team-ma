
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    let uniqueMovies = new Set();

    console.log(resultData);

    // Iterate through resultData
    for (let i = 0; i < resultData.length - 1; i++) {
        let movieId = resultData[i]['movie_id'];

        if (uniqueMovies.has(movieId)) {
            continue;
        }
        // Add the movie ID to the Set to track it as added
        uniqueMovies.add(movieId);

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

    console.log("max pages: ", resultData[resultData.length-1]["max_pages"]);
    handlePagination(resultData[resultData.length-1]["max_pages"]);
}

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


function handlePagination(max_pages) {
    console.log("handling pagination");
    const paginationDiv = document.getElementById('pagination');
    paginationDiv.innerHTML = '';
    const params = new URLSearchParams(window.location.search);
    const current_page = parseInt(params.get("page"));
    if(current_page > 1){
        paginationDiv.innerHTML = '';

        const prevButton = document.createElement('button');
        prevButton.textContent = 'Prev';
        prevButton.onclick = () => sortMoviesFunc(current_page - 1);  // Pass the new page number
        paginationDiv.appendChild(prevButton);
    }

    if(current_page < max_pages){
        const nextButton = document.createElement('button');
        nextButton.textContent = 'Next';
        nextButton.onclick = () => sortMoviesFunc(current_page + 1);  // Pass the new page number
        paginationDiv.appendChild(nextButton);

    }
}

function sortMoviesFunc(page = '1'){
    console.log("edit result params");
    let sort = document.getElementById("sort").value;
    let N = document.getElementById("N").value;
    const params = new URLSearchParams(window.location.search);

    // Get existing parameters
    const title = params.get("title");
    const year = params.get("year");
    const director = params.get("director");
    const star = params.get("star");
    const genre_id = params.get("genre_id");
    const title_letter = params.get("title_letter");

    if(sort){
        params.set("sort", sort);
    } else{
        sort = params.get("sort");
    }

    if(N){
        params.set("N", N);
    } else{
        N = params.get("N");
    }

    params.set("page", page);

    // Update the URL without reloading the page
    window.history.replaceState({}, '', `${window.location.pathname}?${params.toString()}`);

// Makes the HTTP GET request and registers on success callback function handleMovieResult
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies",
        data: { title, year, director, star, genre_id, sort, N, page }, // Send parameters to the server
        success: handleMovieResult,
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("AJAX error: ", textStatus, errorThrown);
        }
    });

}

// Extract query parameters
const params = new URLSearchParams(window.location.search);
const title = params.get("title");
const year = params.get("year");
const director = params.get("director");
const star = params.get("star");

const sort = params.get("sort");
const N = params.get("N");

const genre_id = params.get("genre_id");
const title_letter = params.get("title_letter");

const page = params.get("page");

// // Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    data: { title, year, director, star, genre_id , title_letter , sort, N, page}, // Send parameters to the server
    success: handleMovieResult,
    error: (jqXHR, textStatus, errorThrown) => {
        console.error("AJAX error: ", textStatus, errorThrown);
    }
});


