function displayCart(cart_content) {
    console.log("displayCart");
    console.log(cart_content);
    if (Array.isArray(cart_content)) {
        const cartTableBody = $("#cart-table tbody");
        cartTableBody.empty();

        if (cart_content.length > 0) {  // Check if the array has items
            cart_content.forEach(item => {
                console.log("here");
                console.log(JSON.stringify(item));
                const rowHTML = `
                    <tr>
                        <td>${item.movieTitle}</td>
                        <td>${item.quantity}</td>
                    </tr>`;
                cartTableBody.append(rowHTML);
            });
        } else {
            // Handle the case where the cart is empty
            $("#cart-items").text("Cart is empty");
        }
        console.log("displayCartData ended");
    }
}


$.ajax({
    type: "GET",
    url: "api/cart",
    success: results => {
        displayCart(results);
        console.log("result send back from cartservlet: " + results);
    },
    error: (jqXHR, textStatus, errorThrown) => {
        console.error("AJAX error: ", textStatus, errorThrown);
    }
});
