package hung.demo.service;

import hung.demo.entity.Order;
import hung.demo.entity.OrderDetail;
import hung.demo.entity.Product;
import hung.demo.model.CartItem;
import hung.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional
    public Order checkout(List<CartItem> cartItems) {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());

        List<OrderDetail> details = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            Product product = productService.findById(cartItem.getProductId());
            if (product == null) {
                continue;
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(product.getPrice());
            detail.setAmount(product.getPrice() * cartItem.getQuantity());
            details.add(detail);
            totalAmount += detail.getAmount();
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(details);
        return orderRepository.save(order);
    }
}
