
let star_form = $("#star_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleStarResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add star response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        $("#star_success_message").text(resultDataJson["message"]);
    } else {
        // If payment fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#star_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitStarForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: star_form.serialize(),
            success: handleStarResult
        }
    );
}

// Bind the submit action of the form to a handler function
star_form.submit(submitStarForm);