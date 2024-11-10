
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

/**
 * Handle the items in item list
 * //@param resultArray jsonObject, needs to be parsed to html
 *
function handleCartArray(resultArray) {
    console.log(resultArray);
    let item_list = $("#item_list");
    // change it to html list
    let res = "<ul>";
    for (let i = 0; i < resultArray.size(); i++) {
        // each item will be in a bullet point
        res += "<li>" + resultArray[i] + "</li>";
    }
    res += "</ul>";

    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);
}*/

/**
 * Submit form content with POST method
 * //@param cartEvent
 *
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     *
    cartEvent.preventDefault();

    $.ajax("api/index", {
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["cartItems"]);
        }
    });

    // clear input form
    cart[0].reset();
}*/


$.ajax("api/index", {
    method: "GET",
    success: handleSessionData
});

// Bind the submit action of the form to a event handler function
//cart.submit(handleCartInfo);
