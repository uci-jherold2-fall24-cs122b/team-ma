import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "UpdateCartServlet", urlPatterns = "/api/updatecart")
public class UpdateCartServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        // Step 1: Parse the incoming JSON data from the request body
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String jsonBody = sb.toString();

        // Step 2: Convert the JSON string into a JsonArray
        JsonArray cartItems = new Gson().fromJson(jsonBody, JsonArray.class);

        // Step 3: Store the updated cart back in the session
        session.setAttribute("cartItems", cartItems);

        // Step 4: Log the size of the updated cart
        request.getServletContext().log("Updated cart contains " + cartItems.size() + " items");

        // Step 5: Send the updated cart back as the response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(cartItems.toString());
        response.getWriter().flush();
    }
}
