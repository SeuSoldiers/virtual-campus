package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Order;
import seu.virtualcampus.service.OrderService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        int result = orderService.createOrder(order);
        if (result > 0) {
            return ResponseEntity.ok("Order created successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to create order");
        }
    }

    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable String orderId) {
        int result = orderService.cancelOrder(orderId);
        if (result > 0) {
            return ResponseEntity.ok("Order cancelled successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to cancel order");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(@RequestBody Order order) {
        int result = orderService.updateOrder(order);
        if (result > 0) {
            return ResponseEntity.ok("Order updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update order");
        }
    }

    @GetMapping("/get/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // ========== 新增业务接口 ==========

    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewOrder(@RequestParam String userId, 
                                                           @RequestParam(required = false) List<String> cartItemIds) {
        Map<String, Object> result = orderService.previewOrder(userId, cartItemIds);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable String orderId,
                                                       @RequestParam String userId,
                                                       @RequestParam String accountNumber,
                                                       @RequestParam String password,
                                                       @RequestParam String paymentMethod) {
        Map<String, Object> result = orderService.payOrder(userId, orderId, accountNumber, password, paymentMethod);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<Map<String, Object>> deliverOrder(@PathVariable String orderId,
                                                           @RequestParam String adminId) {
        Map<String, Object> result = orderService.deliverOrder(adminId, orderId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable String orderId,
                                                           @RequestParam String userId) {
        Map<String, Object> result = orderService.confirmOrder(userId, orderId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}/detail")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable String orderId,
                                                             @RequestParam String userId) {
        Map<String, Object> result = orderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(result);
    }
}