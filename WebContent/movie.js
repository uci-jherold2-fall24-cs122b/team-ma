function sendToServlet(title, year, director, star, genre_id, title_letter, sort, N, page){
    // Makes the HTTP GET request and registers on success callback function handleMovieResult
    let data = {};
    if(genre_id === null){
        data = { title, year, director, star, title_letter, sort, N, page };
    }
    else{
        data = { genre_id, sort, N, page };
    }
    console.log("sending: ", data);
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies",
        data: data, // Send parameters to the server
        success: handleMovieResult,
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("AJAX error: ", textStatus, errorThrown);
        }
    });
}
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    console.log(resultData);
    const params = new URLSearchParams(window.location.search);
    const sort = params.get("sort");
    const N = params.get("N");

    jQuery("#sort").val(sort);
    jQuery("#N").val(N);

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    let uniqueMovies = new Set();

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

        rowHTML += "<th><button class='add-to-cart' data-id='" + movieId +
            "' data-title='" + resultData[i]["movie_title"] +
            "'>Add to Cart</button></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }

    $(document).on("click", ".add-to-cart", function () {
        const movieId = $(this).data("id");
        const movieTitle = $(this).data("title");
        const price = $(this).data("price");

        // Call function to add item to cart
        addToCart(movieId, movieTitle,  1, price); // Quantity 1 as default
    });
    console.log("max pages: ", resultData[resultData.length-1]["max_pages"]);
    handlePagination(resultData[resultData.length-1]["max_pages"]);
}

function addToCart(movieId, movieTitle, quantity, price) {
    $.ajax({
        type: "POST",
        url: "api/cart",
        data: {
            movieId: movieId,
            movieTitle: movieTitle,
            quantity: quantity,
            price: price
        },
        success: (response) => {
            console.log("Movie added to cart:", response);
            alert(movieTitle + " has been added to your cart!");
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Failed to add movie to cart:", textStatus, errorThrown);
            alert("Failed to add movie to cart. Please try again.");
        }
    });
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
        prevButton.onclick = () => {
            editMovieList(current_page - 1);
            window.scrollTo(0, 0);  // Scroll to the top
        }  // Pass the new page number
        paginationDiv.appendChild(prevButton);
    }

    if(current_page < max_pages){
        const nextButton = document.createElement('button');
        nextButton.textContent = 'Next';
        nextButton.onclick = () => {
            editMovieList(current_page + 1);
            window.scrollTo(0, 0);  // Scroll to the top

        }  // Pass the new page number
        paginationDiv.appendChild(nextButton);

    }


}

function editMovieList(page){
    console.log("edit result params, page: ", page);
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

    params.set("sort", sort);
    params.set("N", N);

    if(page === undefined){
        page = params.get("page");
    }
    params.set("page", (String)(page));
    console.log("params", params.get("page"));

    // Update the URL without reloading the page
    window.history.replaceState({}, '', `${window.location.pathname}?${params.toString()}`);
    sendToServlet(title, year, director, star, genre_id, sort, N, page);


}

// Extract query parameters
console.log("extracting query params")
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

sendToServlet(title, year, director, star, genre_id, title_letter, sort, N, page);



