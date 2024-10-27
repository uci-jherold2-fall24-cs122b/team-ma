
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
    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.empty();

    console.log("Result Data: ", resultData); // testing
    console.log(resultData[0]["movie_title"]);

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<h2>" + resultData[0]["movie_title"] +"</h2>" +
        "<p>Release Year: " + resultData[0]["movie_year"] +"</p>" +
        "<p>Director: " + resultData[0]["movie_director"] +"</p>" +
        "<p>Genres: " + resultData[0]["movie_genres"].join(', ') + "</p>" +
        "<p>Rating: " + resultData[0]["movie_rating"] +"</p>");

    let addToCartElement = jQuery("#add-to-cart");
    addToCartElement.empty();
    addToCartElement.append(`
    <button class='add-to-cart btn button_top' data-id='${movieId}' data-title='${resultData[0]["movie_title"]}'>
        <i class='fas fa-plus'></i> Add to Cart
    </button>`);

    $(document).on("click", ".add-to-cart", function () {
        const movieId = $(this).data("id");
        const movieTitle = $(this).data("title");
        const price = $(this).data("price");

        // Call function to add item to cart
        addToCart(movieId, movieTitle,  1, price); // Quantity 1 as default
    });
    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    let stars = resultData[0]["stars"];
    if (stars && Array.isArray(stars)) {
        for (let j = 0; j < stars.length; j++) {
            // Add a link to single-star.html with id passed with GET url parameter
            const rowHTML = "<tr><th>" + '<a href=single-star.html?id=' + stars[j]['id'] + '>'
                + stars[j]["name"] +     // display star_name for the link text
                '</a>' + "</th></tr>";

            movieTableBodyElement.append(rowHTML);
        }
    } else {
        movieTableBodyElement.append("<tr><td colspan='3'>No stars found.</td></tr>"); // Fallback message for no movies
    }

    const movieListUrl = document.getElementById('movie-list-url');
    movieListUrl.href = "movie.html?" + resultData[1]["movie_list_url"];

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
/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});