package seu.virtualcampus.ui.shop.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import okhttp3.*;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 管理员商品管理控制器
 */
public class AdminProductsController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminProductsController.class.getName());
    private final String baseUrl = "http://localhost:8080";
    private final int pageSize = 20;
    // 搜索和筛选控件
    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> statusFilter;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addNewButton;
    // 商品列表
    @FXML
    private TableView<ProductModel> productsTable;
    @FXML
    private TableColumn<ProductModel, String> idColumn;
    @FXML
    private TableColumn<ProductModel, String> nameColumn;
    @FXML
    private TableColumn<ProductModel, Double> priceColumn;
    @FXML
    private TableColumn<ProductModel, Integer> stockColumn;
    @FXML
    private TableColumn<ProductModel, String> statusColumn;
    @FXML
    private TableColumn<ProductModel, Void> actionColumn;
    // 分页控件
    @FXML
    private Button prevPageButton;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button nextPageButton;
    // 编辑表单
    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private ChoiceBox<String> statusChoiceBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    // 状态显示
    @FXML
    private Label statusLabel;
    @FXML
    private Label totalCountLabel;
    @FXML
    private Label currentUserLabel;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isEditMode = false;

    private static String normalizeStatus(String backendStatus) {
        if (backendStatus == null) return "OFF";
        String s = backendStatus.trim().toUpperCase();
        if ("ACTIVE".equals(s) || "ON".equals(s)) return "ON";
        return "OFF";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            LOGGER.fine("AdminProductsController 开始初始化");
            LOGGER.fine("location = " + location);
            LOGGER.fine("当前用户角色: " + MainApp.role);
            LOGGER.fine("当前用户名: " + MainApp.username);

            // 延迟初始化，避免因类加载问题导致FXML加载失败
            try {
                LOGGER.fine("初始化 HTTP 客户端");
                this.httpClient = new OkHttpClient();
                LOGGER.fine("HTTP 客户端初始化成功");
            } catch (Throwable t) {
                LOGGER.severe("HTTP 客户端初始化失败: " + t.getMessage());
                showAlert("初始化错误", "网络组件初始化失败: " + t.getMessage());
                return;
            }
            try {
                LOGGER.fine("初始化 JSON 解析器");
                this.objectMapper = new ObjectMapper();
                this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                LOGGER.fine("JSON 解析器初始化成功");
            } catch (Throwable t) {
                LOGGER.severe("JSON 解析器初始化失败: " + t.getMessage());
                showAlert("初始化错误", "JSON组件初始化失败: " + t.getMessage());
                return;
            }

            LOGGER.fine("检查表格组件");
            if (productsTable == null || idColumn == null || nameColumn == null ||
                    priceColumn == null || stockColumn == null || statusColumn == null || actionColumn == null) {
                LOGGER.severe("表格组件未正确加载");
                LOGGER.severe("  productsTable = " + productsTable);
                LOGGER.severe("  idColumn = " + idColumn);
                LOGGER.severe("  nameColumn = " + nameColumn);
                LOGGER.severe("  priceColumn = " + priceColumn);
                LOGGER.severe("  stockColumn = " + stockColumn);
                LOGGER.severe("  statusColumn = " + statusColumn);
                LOGGER.severe("  actionColumn = " + actionColumn);
                showAlert("初始化错误", "表格组件未正确加载");
                return;
            }
            LOGGER.fine("表格组件检查通过");

            LOGGER.fine("检查搜索组件");
            if (searchField == null || statusFilter == null || searchButton == null ||
                    refreshButton == null || addNewButton == null) {
                LOGGER.severe("搜索组件未正确加载");
                LOGGER.severe("  searchField = " + searchField);
                LOGGER.severe("  statusFilter = " + statusFilter);
                LOGGER.severe("  searchButton = " + searchButton);
                LOGGER.severe("  refreshButton = " + refreshButton);
                LOGGER.severe("  addNewButton = " + addNewButton);
                showAlert("初始化错误", "搜索组件未正确加载");
                return;
            }
            LOGGER.fine("搜索组件检查通过");

            LOGGER.fine("检查表单组件");
            if (nameField == null || priceField == null || stockField == null ||
                    statusChoiceBox == null || descriptionArea == null || saveButton == null || clearButton == null) {
                LOGGER.severe("表单组件未正确加载");
                LOGGER.severe("  nameField = " + nameField);
                LOGGER.severe("  priceField = " + priceField);
                LOGGER.severe("  stockField = " + stockField);
                LOGGER.severe("  statusChoiceBox = " + statusChoiceBox);
                LOGGER.severe("  descriptionArea = " + descriptionArea);
                LOGGER.severe("  saveButton = " + saveButton);
                LOGGER.severe("  clearButton = " + clearButton);
                showAlert("初始化错误", "表单组件未正确加载");
                return;
            }
            LOGGER.fine("表单组件检查通过");

            LOGGER.fine("检查状态显示组件");
            if (statusLabel == null || totalCountLabel == null || currentUserLabel == null || pageInfoLabel == null) {
                LOGGER.severe("状态显示组件未正确加载");
                LOGGER.severe("  statusLabel = " + statusLabel);
                LOGGER.severe("  totalCountLabel = " + totalCountLabel);
                LOGGER.severe("  currentUserLabel = " + currentUserLabel);
                LOGGER.severe("  pageInfoLabel = " + pageInfoLabel);
                showAlert("初始化错误", "状态显示组件未正确加载");
                return;
            }
            LOGGER.fine("状态显示组件检查通过");

            LOGGER.fine("检查管理员权限");
            if (!isAdmin()) {
                LOGGER.severe("用户没有管理员权限，当前角色: " + MainApp.role);
                showAlert("权限错误", "您没有管理员权限，无法访问此页面");
                return;
            }
            LOGGER.fine("管理员权限检查通过");

            LOGGER.fine("初始化表格列");
            initializeTableColumns();
            LOGGER.fine("表格列初始化完成");

            LOGGER.fine("初始化事件处理");
            initializeEventHandlers();
            LOGGER.fine("事件处理初始化完成");

            LOGGER.fine("更新用户显示");
            updateCurrentUserDisplay();
            LOGGER.fine("用户显示更新完成");

            LOGGER.fine("加载商品数据");
            loadProducts();
            LOGGER.fine("AdminProductsController 初始化完成");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "AdminProductsController 初始化异常: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
            showAlert("初始化失败", "管理商品界面初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化表格列
     */
    private void initializeTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // 设置价格列格式
        priceColumn.setCellFactory(column -> new TableCell<ProductModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("¥%.2f", item));
                }
            }
        });

        // 设置状态列格式
        statusColumn.setCellFactory(column -> new TableCell<ProductModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("ON".equals(item) ? "上架" : "下架");
                    setStyle("ON".equals(item) ?
                            "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                            "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
        });

        // 设置操作列
        actionColumn.setCellFactory(column -> new TableCell<ProductModel, Void>() {
            private final HBox buttonBox = new HBox(5);
            private final Button editButton = new Button("编辑");
            private final Button deleteButton = new Button("删除");
            private final Button statusButton = new Button("上/下架");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                statusButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12px;");

                buttonBox.getChildren().addAll(editButton, deleteButton, statusButton);

                editButton.setOnAction(event -> {
                    ProductModel product = getTableView().getItems().get(getIndex());
                    editProduct(product);
                });

                deleteButton.setOnAction(event -> {
                    ProductModel product = getTableView().getItems().get(getIndex());
                    deleteProduct(product.getId());
                });

                statusButton.setOnAction(event -> {
                    ProductModel product = getTableView().getItems().get(getIndex());
                    toggleProductStatus(product.getId(), product.getStatus());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    /**
     * 初始化事件处理
     */
    private void initializeEventHandlers() {
        // 初始化ChoiceBox的选项和默认值
        statusFilter.getItems().addAll("全部", "上架", "下架");
        statusFilter.setValue("全部");

        statusChoiceBox.getItems().addAll("ON", "OFF");
        statusChoiceBox.setValue("ON");

        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> handleRefresh());
        addNewButton.setOnAction(event -> handleAddNew());
        prevPageButton.setOnAction(event -> handlePrevPage());
        nextPageButton.setOnAction(event -> handleNextPage());
        saveButton.setOnAction(event -> handleSave());
        clearButton.setOnAction(event -> handleClear());
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin() {
        return "ShopMgr".equalsIgnoreCase(MainApp.role);
    }

    /**
     * 更新当前用户显示
     */
    private void updateCurrentUserDisplay() {
        currentUserLabel.setText("当前用户: " + (MainApp.username != null ? MainApp.username : "未知用户") +
                " | 角色: " + (MainApp.role != null ? MainApp.role : "未知角色"));
    }

    /**
     * 加载商品列表
     */
    private void loadProducts() {
        String searchText = searchField.getText() != null ? searchField.getText().trim() : "";
        String status = getStatusValue(statusFilter.getValue());

        LOGGER.fine("loadProducts 被调用");
        LOGGER.fine("处理后的搜索文本: '" + searchText + "'");
        LOGGER.fine("处理后的状态: '" + status + "'");

        String url = baseUrl + "/api/products?page=" + currentPage + "&size=" + pageSize + "&sort=name,asc";

        if (!searchText.isEmpty()) {
            try {
                String encodedSearchText = java.net.URLEncoder.encode(searchText, "UTF-8");
                url += "&search=" + encodedSearchText;
                LOGGER.fine("添加搜索参数: " + encodedSearchText);
            } catch (java.io.UnsupportedEncodingException e) {
                LOGGER.log(Level.WARNING, "URL编码失败: " + e.getMessage(), e);
                url += "&search=" + searchText;
            }
        }
        if (!"ALL".equals(status)) {
            url += "&status=" + status;
            LOGGER.fine("添加状态参数: " + status);
        }

        LOGGER.fine("最终请求URL: " + url);

        Request.Builder requestBuilder = new Request.Builder().url(url);

        // 添加管理员认证头
        if (MainApp.token != null) {
            requestBuilder.header("Authorization", MainApp.token);
        }
        requestBuilder.header("X-ADMIN-TOKEN", "admin-access");

        Request request = requestBuilder.build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    showAlert("错误", "网络请求失败: " + e.getMessage());
                    statusLabel.setText("加载失败");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // 后端当前返回的是 List<Product>，字段为 productId/productName/productPrice/availableCount/status
                            if (responseBody.trim().startsWith("[")) {
                                java.util.List<BackendProduct> list = objectMapper.readValue(
                                        responseBody,
                                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, BackendProduct.class)
                                );
                                ProductListResponse wrapped = new ProductListResponse();
                                java.util.List<ProductResponse> mapped = new java.util.ArrayList<>();
                                for (BackendProduct bp : list) {
                                    ProductResponse pr = new ProductResponse();
                                    pr.setId(bp.getProductId());
                                    pr.setName(bp.getProductName());
                                    pr.setPrice(bp.getProductPrice());
                                    pr.setStock(bp.getAvailableCount());
                                    pr.setStatus(normalizeStatus(bp.getStatus()));
                                    pr.setDescription(null);
                                    mapped.add(pr);
                                }
                                wrapped.setProducts(mapped);
                                updateProductTable(wrapped);
                            } else {
                                ProductListResponse productListResponse = objectMapper.readValue(responseBody, ProductListResponse.class);
                                updateProductTable(productListResponse);
                            }

                            // 更新总页数
                            String totalCountHeader = response.header("X-Total-Count");
                            if (totalCountHeader != null) {
                                int totalCount = Integer.parseInt(totalCountHeader);
                                totalPages = (int) Math.ceil((double) totalCount / pageSize);
                                updatePageInfo();
                                totalCountLabel.setText("总计: " + totalCount + " 个商品");
                            }

                            statusLabel.setText("加载成功");
                            statusLabel.setStyle("-fx-text-fill: #27ae60;");
                        } catch (Exception e) {
                            showAlert("错误", "解析商品数据失败: " + e.getMessage());
                            statusLabel.setText("解析失败");
                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        }
                    } else {
                        showAlert("错误", "获取商品列表失败: " + responseBody);
                        statusLabel.setText("获取失败");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    }
                });
            }
        });
    }

    /**
     * 更新商品表格
     */
    private void updateProductTable(ProductListResponse response) {
        ObservableList<ProductModel> products = FXCollections.observableArrayList();

        if (response.getProducts() != null) {
            for (ProductResponse product : response.getProducts()) {
                ProductModel model = new ProductModel(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getStatus(),
                        product.getDescription()
                );
                products.add(model);
            }
        }

        productsTable.setItems(products);
    }

    /**
     * 更新分页信息
     */
    private void updatePageInfo() {
        pageInfoLabel.setText(String.format("第 %d 页 / 共 %d 页", currentPage, totalPages));
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    /**
     * 搜索处理
     */
    @FXML
    private void handleSearch() {
        LOGGER.fine("handleSearch 被调用");
        LOGGER.fine("搜索文本: '" + (searchField.getText() != null ? searchField.getText() : "null") + "'");
        LOGGER.fine("状态筛选: '" + (statusFilter.getValue() != null ? statusFilter.getValue() : "null") + "'");
        currentPage = 1;
        loadProducts();
    }

    /**
     * 刷新处理
     */
    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    /**
     * 新增商品处理
     */
    @FXML
    private void handleAddNew() {
        clearForm();
        isEditMode = false;
        statusLabel.setText("新增模式");
        statusLabel.setStyle("-fx-text-fill: #3498db;");
    }

    /**
     * 上一页处理
     */
    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadProducts();
        }
    }

    /**
     * 下一页处理
     */
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadProducts();
        }
    }

    /**
     * 保存处理
     */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            // 后端期望的字段：productName/productPrice/availableCount/status
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("productName", nameField.getText().trim());
            payload.put("productPrice", Double.parseDouble(priceField.getText().trim()));
            payload.put("availableCount", Integer.parseInt(stockField.getText().trim()));
            // 将 UI 的 ON/OFF 转换为后端的 ACTIVE/INACTIVE
            String backendStatus = "ON".equalsIgnoreCase(statusChoiceBox.getValue()) ? "ACTIVE" : "INACTIVE";
            payload.put("status", backendStatus);
            String requestBody = objectMapper.writeValueAsString(payload);

            Request.Builder requestBuilder;
            if (isEditMode && !idField.getText().isEmpty()) {
                // 更新商品
                requestBuilder = new Request.Builder()
                        .url(baseUrl + "/api/products/" + idField.getText())
                        .put(RequestBody.create(requestBody, MediaType.get("application/json")));
            } else {
                // 新增商品
                requestBuilder = new Request.Builder()
                        .url(baseUrl + "/api/products")
                        .post(RequestBody.create(requestBody, MediaType.get("application/json")));
            }

            // 添加认证头
            if (MainApp.token != null) {
                requestBuilder.header("Authorization", MainApp.token);
            }
            requestBuilder.header("X-ADMIN-TOKEN", "admin-access");

            Request request = requestBuilder.build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> {
                        showAlert("错误", "网络请求失败: " + e.getMessage());
                        statusLabel.setText("保存失败");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            showAlert("成功", isEditMode ? "商品更新成功！" : "商品创建成功！");
                            statusLabel.setText(isEditMode ? "更新成功" : "创建成功");
                            statusLabel.setStyle("-fx-text-fill: #27ae60;");
                            clearForm();
                            loadProducts();
                        } else {
                            showAlert("错误", (isEditMode ? "更新" : "创建") + "商品失败: " + responseBody);
                            statusLabel.setText("保存失败");
                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        }
                    });
                }
            });
        } catch (Exception e) {
            showAlert("错误", "数据处理失败: " + e.getMessage());
            statusLabel.setText("数据错误");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    /**
     * 清空表单处理
     */
    @FXML
    private void handleClear() {
        clearForm();
    }

    /**
     * 编辑商品
     */
    private void editProduct(ProductModel product) {
        idField.setText(product.getId());
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        statusChoiceBox.setValue(product.getStatus());
        descriptionArea.setText(product.getDescription());

        isEditMode = true;
        statusLabel.setText("编辑模式 - 商品ID: " + product.getId());
        statusLabel.setStyle("-fx-text-fill: #f39c12;");
    }

    /**
     * 删除商品
     */
    private void deleteProduct(String productId) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("确认删除");
        confirmation.setHeaderText(null);
        confirmation.setContentText("确定要删除这个商品吗？此操作不可撤销。");

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(baseUrl + "/api/products/" + productId)
                        .delete();

                // 添加认证头
                if (MainApp.token != null) {
                    requestBuilder.header("Authorization", MainApp.token);
                }
                requestBuilder.header("X-ADMIN-TOKEN", "admin-access");

                Request request = requestBuilder.build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Platform.runLater(() -> {
                            showAlert("错误", "网络请求失败: " + e.getMessage());
                            statusLabel.setText("删除失败");
                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                showAlert("成功", "商品删除成功！");
                                statusLabel.setText("删除成功");
                                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                                loadProducts();
                            } else {
                                showAlert("错误", "删除商品失败: " + responseBody);
                                statusLabel.setText("删除失败");
                                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 切换商品上下架状态
     */
    private void toggleProductStatus(String productId, String currentStatus) {
        String newStatus = "ON".equals(currentStatus) ? "OFF" : "ON";
        String action = "ON".equals(newStatus) ? "上架" : "下架";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("确认" + action);
        confirmation.setHeaderText(null);
        confirmation.setContentText("确定要" + action + "这个商品吗？");

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String backendStatus = "ON".equals(newStatus) ? "ACTIVE" : "INACTIVE";
                Request.Builder requestBuilder = new Request.Builder()
                        .url(baseUrl + "/api/products/" + productId + "/status?status=" + backendStatus)
                        .put(RequestBody.create("", MediaType.get("application/json")));

                // 添加认证头
                if (MainApp.token != null) {
                    requestBuilder.header("Authorization", MainApp.token);
                }
                requestBuilder.header("X-ADMIN-TOKEN", "admin-access");

                Request request = requestBuilder.build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Platform.runLater(() -> {
                            showAlert("错误", "网络请求失败: " + e.getMessage());
                            statusLabel.setText(action + "失败");
                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                showAlert("成功", "商品" + action + "成功！");
                                statusLabel.setText(action + "成功");
                                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                                loadProducts();
                            } else {
                                showAlert("错误", action + "商品失败: " + responseBody);
                                statusLabel.setText(action + "失败");
                                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 表单验证
     */
    private boolean validateForm() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showAlert("验证错误", "商品名称不能为空");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                showAlert("验证错误", "商品价格不能为负数");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("验证错误", "请输入有效的价格");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                showAlert("验证错误", "库存数量不能为负数");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("验证错误", "请输入有效的库存数量");
            return false;
        }

        return true;
    }

    /**
     * 清空表单
     */
    private void clearForm() {
        idField.clear();
        nameField.clear();
        priceField.clear();
        stockField.clear();
        statusChoiceBox.setValue("ON");
        descriptionArea.clear();
        isEditMode = false;
        statusLabel.setText("表单已清空");
        statusLabel.setStyle("-fx-text-fill: #95a5a6;");
    }

    /**
     * 获取状态值
     */
    private String getStatusValue(String statusText) {
        if ("上架".equals(statusText)) {
            return "ON";
        } else if ("下架".equals(statusText)) {
            return "OFF";
        } else {
            return "ALL";
        }
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 返回上一级（管理员/教务）页面
     */
    @FXML
    private void handleBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", productsTable);
    }

    // 商品模型类
    public static class ProductModel {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty stock;
        private final SimpleStringProperty status;
        private final SimpleStringProperty description;

        public ProductModel(String id, String name, Double price, Integer stock, String status, String description) {
            this.id = new SimpleStringProperty(id != null ? id : "");
            this.name = new SimpleStringProperty(name != null ? name : "");
            this.price = new SimpleDoubleProperty(price != null ? price : 0.0);
            this.stock = new SimpleIntegerProperty(stock != null ? stock : 0);
            this.status = new SimpleStringProperty(status != null ? status : "");
            this.description = new SimpleStringProperty(description != null ? description : "");
        }

        // Getters
        public String getId() {
            return id.get();
        }

        public SimpleStringProperty idProperty() {
            return id;
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public double getPrice() {
            return price.get();
        }

        public SimpleDoubleProperty priceProperty() {
            return price;
        }

        public int getStock() {
            return stock.get();
        }

        public SimpleIntegerProperty stockProperty() {
            return stock;
        }

        public String getStatus() {
            return status.get();
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getDescription() {
            return description.get();
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }
    }

    // 数据传输对象类
    public static class ProductListResponse {
        private List<ProductResponse> products;

        public List<ProductResponse> getProducts() {
            return products;
        }

        public void setProducts(List<ProductResponse> products) {
            this.products = products;
        }
    }

    public static class ProductResponse {
        private String id;
        private String name;
        private Double price;
        private Integer stock;
        private String status;
        private String description;

        // getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class ProductRequest {
        private String name;
        private Double price;
        private Integer stock;
        private String status;
        private String description;

        // getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // 与后端领域对象字段对齐的载体
    public static class BackendProduct {
        private String productId;
        private String productName;
        private Double productPrice;
        private Integer availableCount;
        private String status;
        private String productType;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Double getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(Double productPrice) {
            this.productPrice = productPrice;
        }

        public Integer getAvailableCount() {
            return availableCount;
        }

        public void setAvailableCount(Integer availableCount) {
            this.availableCount = availableCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }
    }
}
