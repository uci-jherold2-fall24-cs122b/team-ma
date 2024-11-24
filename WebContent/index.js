
/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    console.log(resultDataString);
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    //handleCartArray(resultDataJson["previousItems"]);

    // populating genre table
    let genreTableBodyElement = jQuery("#genre_table_body");

    console.log("populating genre table");
    let genres = resultDataJson["genres"];
    console.log(genres);
    if (genres && Array.isArray(genres)) {
        for (let j = 0; j < genres.length; j++) {
            // Check if a new row is needed
            if (j % 5 === 0) {
                // Start a new row for every three genres
                genreTableBodyElement.append("<tr></tr>");
            }

            // Get the current row
            let currentRow = genreTableBodyElement.find("tr").last();

            // Create the cell with the genre link
            const cellHTML = "<td>" + '<a href=movie.html?genre_id=' + genres[j]['genre_id'] +
                '&sort=0&N=10&page=1' + '>' +
                genres[j]["genre_name"] +     // display genre_name for the link text
                '</a>' + "</td>";

            // Append the cell to the current row
            currentRow.append(cellHTML);
        }
    } else {
        genreTableBodyElement.append("<tr><td colspan='3'>No genres found.</td></tr>"); // Fallback message for no genres
    }

    // populating genre table
    let alphaTableBodyElement = jQuery("#alpha_table_body");

    console.log("populating alphabetical title table");

    for (let j = 0; j < 10; j++) {
        if (j % 10 === 0) {
            // Start a new row for every three genres
            alphaTableBodyElement.append("<tr></tr>");
        }

        // Get the current row
        let currentRow = alphaTableBodyElement.find("tr").last();

        const letter = j.toString();

        // Create the cell with the genre link
        const cellHTML = "<td>" + '<a href=movie.html?title_letter=' + letter +
            '&sort=0&N=10&page=1' + '>' +
            letter + '</a>' + "</td>";

        // Append the cell to the current row
        currentRow.append(cellHTML);
    }
    for (let i = 0; i < 27; i++) {
        if (i % 10 === 0) {
            // Start a new row for every three genres
            alphaTableBodyElement.append("<tr></tr>");
        }

        // Get the current row
        let currentRow = alphaTableBodyElement.find("tr").last();
        let letter = String.fromCharCode(65 + i);
        if (i === 26) {
            letter = "*";
        }

        // Create the cell with the genre link
        const cellHTML = "<td>" + '<a href=movie.html?title_letter=' + letter + '&sort=0&N=10&page=1'
        + '>' +
            letter + '</a>' + "</td>";

        // Append the cell to the current row
        currentRow.append(cellHTML);
    }
}


$.ajax("api/index", {
    method: "GET",
    success: handleSessionData
});

// Bind the submit action of the form to a event handler function
//cart.submit(handleCartInfo);

/*
 * CS 122B Project 4. Autocomplete Example.
 *
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 *
 * This example implements the basic features of the autocomplete search, features that are
 *   not implemented are mostly marked as "TODO" in the codebase as a suggestion of how to implement them.
 *
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 *
 */


/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
queryHistory = JSON.parse(localStorage.getItem('queryHistory')) || [];


function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")
    queryHistory = JSON.parse(localStorage.getItem('queryHistory')) || [];

    // TODO: if you want to check past query results first, you can do it here
    console.log(queryHistory);
    let cachedResults = queryHistory.filter(item => item.query.toLowerCase() === query.toLowerCase());
    if (cachedResults.length > 0) {
        console.log("Using cached results for query:", query);
        let cachedResults = queryHistory.filter(item => item.query.toLowerCase() === query.toLowerCase());
        console.log("Using cached results for query:", query);
        console.log(cachedResults[0].results.slice(0, 10));
        doneCallback({
            suggestions: cachedResults[0].results.slice(0, 10)
        });
        return;
    }
    const startTime = performance.now();

    // sending the HTTP GET request to the Java Servlet endpoint movie-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": window.location.pathname.replace(/\/[^/]*$/, "") + "/api/autocomplete?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            const endTime = performance.now(); // End timing here
            const duration = endTime - startTime + 300; // Calculate query duration
            console.log(`Query "${query}" took ${duration.toFixed(2)} ms`);
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")
    if (typeof data === 'string') {
        try {
            data = JSON.parse(data);  // Attempt to parse the string into an object
        } catch (e) {
            console.error("Error parsing JSON response:", e);
            return;
        }
    }
    // Ensure the response is in the expected format (array of movies)
    if (Array.isArray(data)) {
        // Map the data to the format expected by the autocomplete library
        let suggestions = data.map(function(item) {
            return {
                value: item.title,  // Title to be displayed in the autocomplete dropdown
                data: {
                    id: item.id,  // Store the ID for further use
                    title: item.title  // You can add other data if needed
                }
            };
        });
        console.log("THIS IS SUGGESTIONS");
        console.log(suggestions);
        if (!queryHistory.find(item => item.query.toLowerCase() === query.toLowerCase())) {
            queryHistory.push({ query: query, results: suggestions });
            localStorage.setItem('queryHistory', JSON.stringify(queryHistory));
        }

        // Call the doneCallback to return the suggestions to the autocomplete library
        doneCallback({ suggestions: suggestions.slice(0, 10) });
    } else {
        console.log("Invalid data format received:", data);
        // Optionally, handle invalid data (e.g., show an error message to the user)
    }
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    console.log("you select " + suggestion["title"] + " with ID " + suggestion["data"]["id"])
    window.location.href = window.location.pathname.replace(/\/[^/]*$/, "") + "/single-movie.html?id=" + suggestion["data"]["id"] + "&sort=0&N=10&page=1";
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        if (query.length < 3) {
            console.log("Query too short, minimum 3 characters");
            return;  // Do not perform the lookup if the query is too short
        }
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    window.location.href = window.location.pathname.replace(/\/[^/]*$/, "") + "/movie.html?title=" + query + "&sort=0&N=10&page=1";
    // TODO: you should do normal search here
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button


