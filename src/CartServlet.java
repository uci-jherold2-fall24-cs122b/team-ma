import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonArray cartItems = (JsonArray) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new JsonArray();
            session.setAttribute("cartItems", cartItems);
        }
        request.getServletContext().log("getting " + cartItems.size() + " items");


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(cartItems.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        JsonArray cartItems = (JsonArray) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new JsonArray();
            session.setAttribute("cartItems", cartItems);
        }
        request.getServletContext().log("getting " + cartItems.size() + " items");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String movieId = request.getParameter("movieId");
        String movieTitle = request.getParameter("movieTitle");

        boolean itemFound = false;

        for (int i = 0; i < cartItems.size(); i++) {
            JsonObject existingItem = cartItems.get(i).getAsJsonObject();
            if (existingItem.get("movieId").getAsString().equals(movieId)) {
                int existingQuantity = existingItem.get("quantity").getAsInt();
                existingItem.addProperty("quantity", existingQuantity + 1); // Increment quantity
                itemFound = true;
                break; // Exit loop after updating
            }
        }

        // If the item was not found, create a new JSON object for the new cart item
        if (!itemFound) {
            JsonObject newItem = new JsonObject();
            newItem.addProperty("movieId", movieId);
            newItem.addProperty("movieTitle", movieTitle);
            newItem.addProperty("quantity", 1);
            newItem.addProperty("price", (int)(Math.random() * (4) + 2));
            cartItems.add(newItem);
        }


        // Write the response
        response.getWriter().print(cartItems.toString());
        response.getWriter().flush();
    }
}
