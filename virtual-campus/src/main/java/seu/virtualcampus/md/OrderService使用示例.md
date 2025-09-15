// OrderService 使用示例

/*
使用方法示例：

1. 预览订单
Map<String, Object> preview = orderService.previewOrder("userId123", Arrays.asList("cart1", "cart2"));
if ((Boolean) preview.get("success")) {
    System.out.println("预览总价: " + preview.get("finalAmount"));
    System.out.println("是否学生: " + preview.get("isStudent"));
}

2. 创建订单
Map<String, Object> createResult = orderService.createOrder("userId123", Arrays.asList("cart1", "cart2"));
if ((Boolean) createResult.get("success")) {
    String orderId = (String) createResult.get("orderId");
    System.out.println("订单创建成功，订单号: " + orderId);
}

3. 支付订单
Map<String, Object> payResult = orderService.payOrder("userId123", "orderId", "accountNumber", "password", "ONLINE");
if ((Boolean) payResult.get("success")) {
    System.out.println("支付成功");
}

4. 确认订单
Map<String, Object> confirmResult = orderService.confirmOrder("userId123", "orderId");
if ((Boolean) confirmResult.get("success")) {
    System.out.println("订单确认成功");
}

5. 取消订单
Map<String, Object> cancelResult = orderService.cancelOrder("userId123", "orderId");
if ((Boolean) cancelResult.get("success")) {
    System.out.println("订单取消成功");
}

6. 获取订单详情
Map<String, Object> detail = orderService.getOrderDetail("userId123", "orderId");
if ((Boolean) detail.get("success")) {
    Order order = (Order) detail.get("order");
    List<Map<String, Object>> items = (List<Map<String, Object>>) detail.get("orderItems");
    System.out.println("订单状态: " + order.getStatus());
    System.out.println("订单项数量: " + items.size());
}

订单状态流转：
PENDING (待支付) -> PAID (已支付) -> SHIPPED (已发货) -> COMPLETED (已完成)
                              \
                               -> CANCELLED (已取消，仅在PENDING状态可取消)
*/
