package hung.demo.controller;

import hung.demo.entity.Order;
import hung.demo.model.CartItem;
import hung.demo.service.CartService;
import hung.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> cartItems = cartService.getCartItems(session);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", cartService.getTotalAmount(session));
        model.addAttribute("cartItemCount", cartService.getTotalQuantity(session));
        return "cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Integer productId,
                             @RequestParam int quantity) {
        return "redirect:/cart/update/" + productId + "/" + quantity;
    }

    @GetMapping("/cart/update/{productId}/{quantity}")
    public String updateCartGet(@PathVariable Integer productId,
                                @PathVariable int quantity,
                                HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeItem(@PathVariable Integer productId, HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        List<CartItem> cartItems = cartService.getCartItems(session);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Gio hang dang rong.");
            return "redirect:/cart";
        }

        Order order = orderService.checkout(cartItems);
        cartService.clear(session);
        redirectAttributes.addFlashAttribute("message", "Dat hang thanh cong. Ma don hang: " + order.getId());
        return "redirect:/cart";
    }
}
