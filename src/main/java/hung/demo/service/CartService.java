package hung.demo.service;

import hung.demo.entity.Product;
import hung.demo.model.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";

    @SuppressWarnings("unchecked")
    private Map<Integer, CartItem> getCartMap(HttpSession session) {
        Object cart = session.getAttribute(CART_SESSION_KEY);
        if (cart instanceof Map<?, ?> existingCart) {
            return (Map<Integer, CartItem>) existingCart;
        }

        Map<Integer, CartItem> newCart = new LinkedHashMap<>();
        session.setAttribute(CART_SESSION_KEY, newCart);
        return newCart;
    }

    public void addToCart(HttpSession session, Product product, int quantity) {
        Map<Integer, CartItem> cart = getCartMap(session);
        CartItem item = cart.get(product.getId());
        if (item == null) {
            cart.put(product.getId(), new CartItem(product.getId(), product.getName(), product.getPrice(), quantity));
            return;
        }
        item.setQuantity(item.getQuantity() + quantity);
    }

    public List<CartItem> getCartItems(HttpSession session) {
        return new ArrayList<>(getCartMap(session).values());
    }

    public void updateQuantity(HttpSession session, Integer productId, int quantity) {
        Map<Integer, CartItem> cart = getCartMap(session);
        if (!cart.containsKey(productId)) {
            return;
        }
        if (quantity <= 0) {
            cart.remove(productId);
            return;
        }
        cart.get(productId).setQuantity(quantity);
    }

    public void removeItem(HttpSession session, Integer productId) {
        getCartMap(session).remove(productId);
    }

    public double getTotalAmount(HttpSession session) {
        return getCartItems(session).stream()
                .mapToDouble(CartItem::getAmount)
                .sum();
    }

    public int getTotalQuantity(HttpSession session) {
        return getCartItems(session).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clear(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}
