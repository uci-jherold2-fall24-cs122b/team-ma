function displayCart(cart_content) {
    console.log("displayCart");
    console.log(cart_content);
    if (Array.isArray(cart_content)) {
        const cartTableBody = $("#cart-table tbody");
        cartTableBody.empty();
        let totalPrice = 0;

        if (cart_content.length > 0) {  // Check if the array has items
            cart_content.forEach(item => {
                console.log("here");
                console.log(JSON.stringify(item));
                const rowHTML = `
                    <tr>
                        <td>${item.movieTitle}</td>
                        <td>${item.quantity}</td>
                        <td>${item.price}</td>
                        <td>
                            <button class="decrease-quantity" data-movie-id="${item.movieId}">-</button>
                            ${item.quantity}
                            <button class="increase-quantity" data-movie-id="${item.movieId}">+</button>
                        </td>
                        <td>
                            <button class="delete-item" data-movie-id="${item.movieId}">Delete</button>
                        </td>
                    </tr>`;
                cartTableBody.append(rowHTML);

                totalPrice += item.price * item.quantity;
            });
            const totalRowHTML = `
                <tr>
                    <td colspan="2"><strong>Total</strong></td>
                    <td colspan="3"><strong>$${totalPrice.toFixed(2)}</strong></td>
                </tr>`;
            cartTableBody.append(totalRowHTML);
        } else {
            $("#cart-items").text("Cart is empty");
        }
        console.log("displayCartData ended");

        $(".increase-quantity").click(function () {
            const movieId = $(this).data("movie-id");
            updateQuantity(movieId, 1);
        });

        $(".decrease-quantity").click(function () {
            const movieId = $(this).data("movie-id");
            updateQuantity(movieId, -1);
        });

        $(".delete-item").click(function () {
            const movieId = $(this).data("movie-id");
            deleteItem(movieId);
        });
    }

    function updateQuantity(movieId, change) {
        const item = cart_content.find(item => item.movieId === movieId);
        if (item) {
            item.quantity += change;

            if (item.quantity < 1) {
                deleteItem(movieId);
            } else {
                displayCart(cart_content);
            }
        }
    }

    function deleteItem(movieId) {
        cart_content = cart_content.filter(item => item.movieId !== movieId);

        displayCart(cart_content);
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
