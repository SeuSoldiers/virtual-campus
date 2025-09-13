package seu.virtualcampus.service;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import seu.virtualcampus.domain.*;
import seu.virtualcampus.mapper.OrderMapper;
import seu.virtualcampus.mapper.OrderItemMapper;
import seu.virtualcampus.mapper.ProductMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CartService cartService;
    
    // 优先使用同进程服务调用，避免自调HTTP的不确定性；必要时仍可回退到HTTP调用
    @Autowired(required = false)
    private BankAccountService bankAccountService;
    
    // HTTP客户端用于调用银行接口
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String bankApiBaseUrl = "http://localhost:8080/api/accounts";

    // 商店收款账户（从配置获取）
    @Value("${shop.merchant.account:}")
    private String merchantAccount;
    
    @Autowired(required = false)
    private StudentInfoService studentInfoService;

    /**
     * 预览订单 - 计算总价但不创建订单
     */
    public Map<String, Object> previewOrder(String userId, List<String> cartItemIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取购物车项
            List<Cart> cartItems;
            if (cartItemIds != null && !cartItemIds.isEmpty()) {
                cartItems = cartService.getCartItemsByIds(cartItemIds);
            } else {
                cartItems = cartService.getCartItemsByUserId(userId);
            }
            
            if (cartItems.isEmpty()) {
                result.put("success", false);
                result.put("message", "购物车为空");
                return result;
            }
            
            // 计算总价
            BigDecimal totalAmount = calculateTotalAmount(cartItems);
            
            // 检查学生折扣
            BigDecimal discountRate = getStudentDiscountRate(userId);
            BigDecimal finalAmount = totalAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            
            List<Map<String, Object>> orderItems = new ArrayList<>();
            for (Cart cart : cartItems) {
                Product product = productMapper.selectById(cart.getProductId());
                if (product == null) continue;
                
                Map<String, Object> item = new HashMap<>();
                item.put("productId", product.getProductId());
                item.put("productName", product.getProductName());
                item.put("quantity", cart.getQuantity());
                item.put("unitPrice", product.getProductPrice());
                item.put("subtotal", BigDecimal.valueOf(product.getProductPrice())
                    .multiply(BigDecimal.valueOf(cart.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP));
                orderItems.add(item);
            }
            
            result.put("success", true);
            result.put("orderItems", orderItems);
            result.put("originalAmount", totalAmount);
            result.put("discountRate", discountRate);
            result.put("finalAmount", finalAmount);
            result.put("isStudent", discountRate.compareTo(BigDecimal.ONE) < 0);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "预览订单失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 创建订单
     */
    @Transactional
    public Map<String, Object> createOrder(String userId, List<String> cartItemIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取购物车项
            List<Cart> cartItems;
            if (cartItemIds != null && !cartItemIds.isEmpty()) {
                cartItems = cartService.getCartItemsByIds(cartItemIds);
            } else {
                cartItems = cartService.getCartItemsByUserId(userId);
            }
            
            if (cartItems.isEmpty()) {
                result.put("success", false);
                result.put("message", "购物车为空");
                return result;
            }
            
            // 验证库存
            for (Cart cart : cartItems) {
                Product product = productMapper.selectById(cart.getProductId());
                if (product == null) {
                    result.put("success", false);
                    result.put("message", "商品不存在: " + cart.getProductId());
                    return result;
                }
                if (product.getAvailableCount() < cart.getQuantity()) {
                    result.put("success", false);
                    result.put("message", "商品库存不足: " + product.getProductName());
                    return result;
                }
            }
            
            // 计算总价
            BigDecimal totalAmount = calculateTotalAmount(cartItems);
            BigDecimal discountRate = getStudentDiscountRate(userId);
            BigDecimal finalAmount = totalAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            
            // 生成订单号
            String orderId = generateOrderId();
            
            // 创建订单
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setTotalAmount(finalAmount.doubleValue());
            order.setStatus("PENDING");
            order.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            order.setPaymentMethod("");
            order.setPaymentStatus("UNPAID");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            orderMapper.insert(order);
            
            // 创建订单项
            List<OrderItem> orderItems = new ArrayList<>();
            for (Cart cart : cartItems) {
                Product product = productMapper.selectById(cart.getProductId());
                
                OrderItem orderItem = new OrderItem();
                orderItem.setItemId(UUID.randomUUID().toString().replace("-", ""));
                orderItem.setOrderId(orderId);
                orderItem.setQuantity(cart.getQuantity());
                orderItem.setProductId(cart.getProductId());
                orderItem.setUnitPrice(product.getProductPrice());
                orderItem.setSubtotal(BigDecimal.valueOf(product.getProductPrice())
                    .multiply(BigDecimal.valueOf(cart.getQuantity()))
                    .multiply(discountRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
                
                orderItems.add(orderItem);
            }
            
            // 批量插入订单项
            orderItemMapper.insertBatch(orderItems);
            
            // 注意：不在创建订单时清空购物车，而是在支付成功后清空
            // 这样用户可以在支付前取消订单而不影响购物车
            
            result.put("success", true);
            result.put("orderId", orderId);
            result.put("totalAmount", finalAmount);
            result.put("message", "订单创建成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建订单失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 支付订单
     */
    @Transactional
    public Map<String, Object> payOrder(String userId, String orderId, String accountNumber, String password, String paymentMethod) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== 开始处理支付请求 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("订单ID: " + orderId);
        System.out.println("账户号: " + accountNumber);
        System.out.println("支付方式: " + paymentMethod);
        
        try {
            // 验证订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                System.out.println("错误: 订单不存在");
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            System.out.println("订单信息: " + order.toString());
            System.out.println("订单状态: " + order.getStatus());
            System.out.println("订单金额: " + order.getTotalAmount());
            
            if (!order.getUserId().equals(userId)) {
                System.out.println("错误: 用户ID不匹配 - 订单用户: " + order.getUserId() + ", 请求用户: " + userId);
                result.put("success", false);
                result.put("message", "无权限访问此订单");
                return result;
            }
            
            if (!"PENDING".equals(order.getStatus())) {
                System.out.println("错误: 订单状态不允许支付 - 当前状态: " + order.getStatus());
                result.put("success", false);
                result.put("message", "订单状态不允许支付");
                return result;
            }
            
            // 校验商家收款账户
            System.out.println("商家收款账户: " + merchantAccount);
            if (merchantAccount == null || merchantAccount.isBlank()) {
                System.out.println("错误: 商家收款账户未配置");
                result.put("success", false);
                result.put("message", "商家收款账户未配置，请联系管理员设置 shop.merchant.account");
                return result;
            }

            // 计算支付金额
            BigDecimal paymentAmount = BigDecimal.valueOf(order.getTotalAmount());
            System.out.println("支付金额: " + paymentAmount);

            // 先扣减库存（在同一事务内，支付失败将回滚库存变更）
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
            System.out.println("订单项数量: " + orderItems.size());
            for (OrderItem item : orderItems) {
                System.out.println("检查库存 - 商品ID: " + item.getProductId() + ", 需要数量: " + item.getQuantity());
                int updateResult = productMapper.reduceStock(item.getProductId(), item.getQuantity());
                if (updateResult == 0) {
                    System.out.println("错误: 库存不足 - 商品ID: " + item.getProductId());
                    result.put("success", false);
                    result.put("message", "库存不足，无法完成支付");
                    return result;
                }
                System.out.println("库存扣减成功 - 商品ID: " + item.getProductId());
            }

            // 根据支付方式调用银行模块：优先使用本地服务调用，失败时回退HTTP
            System.out.println("开始调用银行接口...");
            try {
                if (bankAccountService != null) {
                    if ("先用后付".equals(paymentMethod)) {
                        System.out.println("[Bank] 使用本地服务调用: processPayLater");
                        bankAccountService.processPayLater(accountNumber, password, merchantAccount, paymentAmount);
                    } else {
                        System.out.println("[Bank] 使用本地服务调用: processShopping");
                        bankAccountService.processShopping(accountNumber, password, merchantAccount, paymentAmount);
                    }
                } else {
                    System.out.println("[Bank] 本地服务不可用，回退HTTP调用");
                    if ("先用后付".equals(paymentMethod)) {
                        System.out.println("调用先用后付接口 - /api/accounts/paylater");
                        callBankApi("/paylater", accountNumber, password, merchantAccount, paymentAmount);
                    } else {
                        System.out.println("调用立即付款接口 - /api/accounts/shopping");
                        callBankApi("/shopping", accountNumber, password, merchantAccount, paymentAmount);
                    }
                }
                System.out.println("银行扣款处理完成");
            } catch (Exception e) {
                System.out.println("银行接口调用失败: " + e.getMessage());
                e.printStackTrace();
                // 回滚库存变更
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                result.put("success", false);
                result.put("message", "支付失败: " + e.getMessage());
                return result;
            }
            
            // 更新订单状态
            System.out.println("开始更新订单状态...");
            order.setStatus("PAID");
            order.setPaymentStatus("PAID");
            order.setPaymentMethod(paymentMethod);
            order.setUpdatedAt(LocalDateTime.now());
            int updateResult = orderMapper.update(order);
            System.out.println("订单状态更新结果: " + updateResult);
            
            // 支付成功后清空购物车
            System.out.println("开始清空购物车... userId=" + userId);
            try {
                int cleared = cartService.clearUserCart(userId);
                System.out.println("购物车清空成功, 受影响记录数=" + cleared);
            } catch (Exception e) {
                System.out.println("清空购物车失败: " + e.getMessage());
                // 清空购物车失败不影响支付结果，仅记录日志
                System.err.println("支付成功但清空购物车失败: " + e.getMessage());
            }
            
            result.put("success", true);
            result.put("message", "支付成功");
            System.out.println("=== 支付处理完成，返回成功结果 ===");
            
        } catch (Exception e) {
            System.out.println("支付处理异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "支付处理失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 确认订单（发货后确认收货）
     */
    @Transactional
    public Map<String, Object> confirmOrder(String userId, String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权限访问此订单");
                return result;
            }
            
            if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
                result.put("success", false);
                result.put("message", "订单状态不允许确认");
                return result;
            }
            
            // 更新订单状态为已完成
            order.setStatus("COMPLETED");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.update(order);
            
            result.put("success", true);
            result.put("message", "订单确认成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "确认订单失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 发货订单
     */
    @Transactional
    public Map<String, Object> deliverOrder(String adminId, String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // TODO: 校验管理员身份（后期接统一身份认证）
            // AdminService.validateAdmin(adminId);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (!"PAID".equals(order.getStatus())) {
                result.put("success", false);
                result.put("message", "只能发货已支付的订单");
                return result;
            }
            
            // 更新订单状态为已发货
            order.setStatus("SHIPPED");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.update(order);
            
            result.put("success", true);
            result.put("message", "订单发货成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发货失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 取消订单
     */
    @Transactional
    public Map<String, Object> cancelOrder(String userId, String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权限访问此订单");
                return result;
            }
            
            if (!"PENDING".equals(order.getStatus())) {
                result.put("success", false);
                result.put("message", "只能取消未支付的订单");
                return result;
            }
            
            // 更新订单状态为已取消
            order.setStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.update(order);
            
            result.put("success", true);
            result.put("message", "订单取消成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "取消订单失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取订单详情
     */
    public Map<String, Object> getOrderDetail(String userId, String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权限访问此订单");
                return result;
            }
            
            // 获取订单项
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
            List<Map<String, Object>> itemDetails = new ArrayList<>();
            
            for (OrderItem item : orderItems) {
                Product product = productMapper.selectById(item.getProductId());
                Map<String, Object> itemDetail = new HashMap<>();
                itemDetail.put("itemId", item.getItemId());
                itemDetail.put("productId", item.getProductId());
                itemDetail.put("productName", product != null ? product.getProductName() : "商品已下架");
                itemDetail.put("quantity", item.getQuantity());
                itemDetail.put("unitPrice", item.getUnitPrice());
                itemDetail.put("subtotal", item.getSubtotal());
                itemDetails.add(itemDetail);
            }
            
            result.put("success", true);
            result.put("order", order);
            result.put("orderItems", itemDetails);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取订单详情失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 调用银行账户API接口
     */
    private void callBankApi(String endpoint, String fromAccount, String password, String toAccount, BigDecimal amount) throws IOException {
        String url = bankApiBaseUrl + endpoint + "?fromAccount=" + fromAccount 
                   + "&password=" + password + "&toAccount=" + toAccount + "&amount=" + amount;
        
        System.out.println("发起银行API请求: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.get("application/json")))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            System.out.println("银行API响应: " + response.code() + " - " + responseBody);
            
            if (!response.isSuccessful()) {
                throw new RuntimeException("银行API调用失败: " + responseBody);
            }
        }
    }
    
    // ========== 原有方法保留 ==========
    
    public int createOrder(Order order) {
        return orderMapper.insert(order);
    }

    public int cancelOrder(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());
            return orderMapper.update(order);
        }
        return 0;
    }

    public int updateOrder(Order order) {
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.update(order);
    }

    public Order getOrderById(String orderId) {
        return orderMapper.selectById(orderId);
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderMapper.selectByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
    }

    public int updateOrderStatus(String orderId, String status, String paymentStatus) {
        return orderMapper.updateStatus(orderId, status, paymentStatus);
    }

    // ========== 私有辅助方法 ==========

    private BigDecimal calculateTotalAmount(List<Cart> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (Cart cart : cartItems) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                BigDecimal itemTotal = BigDecimal.valueOf(product.getProductPrice())
                    .multiply(BigDecimal.valueOf(cart.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getStudentDiscountRate(String userId) {
        try {
            if (studentInfoService != null) {
                // 假设 userId 可以转换为 studentId，或者需要额外的映射逻辑
                // 这里简化处理，实际项目中需要建立用户和学生的关联关系
                // StudentInfo student = studentInfoService.getStudentInfo(Long.valueOf(userId));
                // if (student != null && "ACTIVE".equals(student.getStatus())) {
                //     return BigDecimal.valueOf(0.95); // 95折
                // }
                
                // 由于当前 StudentInfo 没有 status 字段，暂时返回原价
                return BigDecimal.ONE;
            }
        } catch (Exception e) {
            // 如果学生服务不可用，按原价计算
        }
        
        return BigDecimal.ONE; // 原价，无折扣
    }

    private String generateOrderId() {
        return "ORDER" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }
}
