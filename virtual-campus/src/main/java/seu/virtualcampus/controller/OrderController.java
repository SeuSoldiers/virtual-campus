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

    // 新版：根据购物车创建订单
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam String userId,
                                                           @RequestParam(required = false) List<String> cartItemIds) {
        Map<String, Object> result = orderService.createOrder(userId, cartItemIds);
        return ResponseEntity.ok(result);
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

    // 兼容UI：POST 取消订单（可选传 userId 进行权限校验）
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrderNew(@PathVariable String orderId,
                                                             @RequestParam(required = false) String userId) {
        Map<String, Object> result = new java.util.HashMap<>();
        if (userId != null && !userId.isEmpty()) {
            result = orderService.cancelOrder(userId, orderId);
        } else {
            int r = orderService.cancelOrder(orderId);
            result.put("success", r > 0);
            result.put("message", r > 0 ? "订单取消成功" : "取消订单失败");
        }
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
                                                           @RequestParam(required = false) String userId) {
        // 兼容未传 userId 的情况：根据订单获取所属用户
        if (userId == null || userId.isEmpty()) {
            Order o = orderService.getOrderById(orderId);
            if (o != null) {
                userId = o.getUserId();
            }
        }
        Map<String, Object> result = orderService.confirmOrder(userId, orderId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}/detail")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable String orderId,
                                                             @RequestParam String userId) {
        Map<String, Object> result = orderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(result);
    }

    // 兼容前端：GET /api/orders/{orderId} 返回扁平结构，便于 OrderDetailController 直接解析
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetailFlat(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> data = orderService.getOrderDetail(order.getUserId(), orderId);
        if (!Boolean.TRUE.equals(data.get("success"))) {
            return ResponseEntity.badRequest().body(data);
        }

        Map<String, Object> body = new java.util.HashMap<>();
        // 兼容前端 Long id：从字符串订单号中提取数字部分作为展示ID
        Long numericId = null;
        try {
            String digits = order.getOrderId() != null ? order.getOrderId().replaceAll("\\D", "") : null;
            if (digits != null && !digits.isEmpty()) {
                numericId = Long.parseLong(digits);
            }
        } catch (Exception ignore) {}
        body.put("id", numericId);
        // userId 既可能是字符串也可能是数字，尽量按数字返回
        try { body.put("userId", Long.valueOf(order.getUserId())); } catch (Exception ignore) { body.put("userId", order.getUserId()); }
        body.put("status", order.getStatus());
        body.put("totalAmount", order.getTotalAmount());
        body.put("paymentMethod", order.getPaymentMethod());
        // 尽量提供创建时间
        body.put("createdAt", order.getOrderDate() != null ? order.getOrderDate() : (order.getCreatedAt() != null ? order.getCreatedAt().toString() : null));
        body.put("paidAt", null);
        body.put("deliveredAt", null);
        body.put("confirmedAt", null);

        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        Object itemsRaw = data.get("orderItems");
        if (itemsRaw instanceof java.util.List) {
            for (Object o : (java.util.List<?>) itemsRaw) {
                if (o instanceof java.util.Map) {
                    java.util.Map<?, ?> it = (java.util.Map<?, ?>) o;
                    java.util.Map<String, Object> i = new java.util.HashMap<>();
                    i.put("productId", it.get("productId"));
                    i.put("productName", it.get("productName"));
                    // OrderDetailController 使用字段名 price
                    i.put("price", it.get("unitPrice"));
                    i.put("quantity", it.get("quantity"));
                    i.put("subtotal", it.get("subtotal"));
                    items.add(i);
                }
            }
        }
        body.put("items", items);
        return ResponseEntity.ok(body);
    }
}
