package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Order;
import seu.virtualcampus.service.OrderService;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器。
 * <p>
 * 提供与商品订单相关的API接口，包括创建、取消、更新、查询订单，以及支付、发货、确认收货等订单流程操作。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * 根据购物车项目创建新订单。
     *
     * @param userId      用户ID。
     * @param cartItemIds 购物车项目ID列表（可选）。如果为空，则使用该用户购物车中的所有商品。
     * @return 包含创建结果（如订单ID）的Map。
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam String userId,
                                                           @RequestParam(required = false) List<String> cartItemIds) {
        Map<String, Object> result = orderService.createOrder(userId, cartItemIds);
        return ResponseEntity.ok(result);
    }

    /**
     * (旧) 取消一个订单。
     *
     * @param orderId 要取消的订单ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable String orderId) {
        int result = orderService.cancelOrder(orderId);
        if (result > 0) {
            return ResponseEntity.ok("Order cancelled successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to cancel order");
        }
    }

    /**
     * 更新一个订单的信息。
     *
     * @param order 包含更新信息的订单对象。
     * @return 操作结果的消息。
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(@RequestBody Order order) {
        int result = orderService.updateOrder(order);
        if (result > 0) {
            return ResponseEntity.ok("Order updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to update order");
        }
    }

    /**
     * (旧) 根据ID获取订单信息。
     *
     * @param orderId 订单ID。
     * @return 订单对象；如果未找到则返回404。
     */
    @GetMapping("/get/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取指定用户的所有订单。
     *
     * @param userId 用户ID。
     * @return 该用户的订单列表。
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * (管理员) 获取所有订单。
     *
     * @return 系统中的所有订单列表。
     */
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // ========== 新增业务接口 ========== 

    /**
     * 预览订单信息。
     * <p>
     * 在正式创建订单前，根据购物车内容计算订单总额、商品列表等信息。
     *
     * @param userId      用户ID。
     * @param cartItemIds 购物车项目ID列表（可选）。如果为空，则预览整个购物车的订单。
     * @return 包含订单预览信息的Map。
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewOrder(@RequestParam String userId, 
                                                           @RequestParam(required = false) List<String> cartItemIds) {
        Map<String, Object> result = orderService.previewOrder(userId, cartItemIds);
        return ResponseEntity.ok(result);
    }

    /**
     * (新) 取消订单。
     * <p>
     * 兼容通过POST请求取消订单，并可选地校验用户ID。
     *
     * @param orderId 要取消的订单ID。
     * @param userId  用户ID（可选），用于权限校验。
     * @return 包含操作结果的Map。
     */
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

    /**
     * 支付订单。
     *
     * @param orderId       要支付的订单ID。
     * @param userId        用户ID。
     * @param accountNumber 支付用的银行账户号码。
     * @param password      银行账户密码。
     * @param paymentMethod 支付方式（如 "DEBIT_CARD", "CREDIT_CARD"）。
     * @return 包含支付结果的Map。
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable String orderId,
                                                       @RequestParam String userId,
                                                       @RequestParam String accountNumber,
                                                       @RequestParam String password,
                                                       @RequestParam String paymentMethod) {
        Map<String, Object> result = orderService.payOrder(userId, orderId, accountNumber, password, paymentMethod);
        return ResponseEntity.ok(result);
    }

    /**
     * (管理员) 对订单进行发货操作。
     *
     * @param orderId 订单ID。
     * @param adminId 操作的管理员ID（可选）。
     * @param token   管理员的认证Token（可选），用于在缺少adminId时作为备用标识。
     * @return 包含操作结果的Map。
     */
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<Map<String, Object>> deliverOrder(@PathVariable String orderId,
                                                           @RequestParam(required = false) String adminId,
                                                           @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("[OrderController] deliverOrder called. orderId={}, adminId={}, tokenPresent={}", orderId, adminId, token != null);
        if (adminId == null || adminId.isBlank()) {
            // 无统一认证，这里仅兜底提供一个非空adminId，避免因缺参报400
            adminId = token != null ? token : "system";
            log.info("[OrderController] adminId missing, fallback to {}", adminId.equals("system") ? "system" : "token");
        }
        Map<String, Object> result = orderService.deliverOrder(adminId, orderId);
        log.info("[OrderController] deliverOrder result: {}", result);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户确认收货。
     *
     * @param orderId 订单ID。
     * @param userId  用户ID（可选）。如果为空，会尝试从订单信息中获取。
     * @return 包含操作结果的Map。
     */
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

    /**
     * 获取订单的详细信息，包括订单下的所有商品项。
     *
     * @param orderId 订单ID。
     * @param userId  用户ID，用于权限验证。
     * @return 包含订单详细信息的Map。
     */
    @GetMapping("/{orderId}/detail")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable String orderId,
                                                             @RequestParam String userId) {
        Map<String, Object> result = orderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * (兼容接口) 获取扁平化的订单详情。
     * <p>
     * 此接口主要为了兼容旧版前端UI (OrderDetailController) 的数据结构要求。
     * 它将订单和订单项的数据整合到一个扁平的Map结构中。
     *
     * @param orderId 订单ID。
     * @return 扁平化的订单详情；如果订单不存在或获取失败，返回相应的错误响应。
     */
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