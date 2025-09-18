package seu.virtualcampus.ui.aichat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seu.virtualcampus.domain.AiMessage;
import seu.virtualcampus.domain.AiSession;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AiChatController implements Initializable {
    private static final Logger logger = Logger.getLogger(AiChatController.class.getName());
    private final String BASE_URL = "http://" + MainApp.host + "/api/ai-chat";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<AiSession> sessionList = FXCollections.observableArrayList();
    private final ObservableList<AiMessage> messageList = FXCollections.observableArrayList();
    @FXML
    public Button backButton;
    @FXML
    private ListView<AiSession> sessionListView;
    @FXML
    private ListView<AiMessage> messageListView;
    @FXML
    private TextField inputField;
    @FXML
    private Button sendBtn;
    @FXML
    private Button createSessionBtn;
    @FXML
    private Label userLabel;
    @FXML
    private Label sessionTitle;
    private Integer currentSessionId = null;
    private String username = "";
    private String token = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("初始化AI聊天界面");
        username = MainApp.username;
        token = MainApp.token;
        userLabel.setText("用户：" + username);

        // 配置会话列表显示
        sessionListView.setItems(sessionList);
        sessionListView.setCellFactory(list -> {
            ListCell<AiSession> cell = new ListCell<>() {
                private final HBox hbox = new HBox();
                private final Label label = new Label();
                private final VBox wrapper = new VBox();

                {
                    label.setWrapText(true);
                    label.setStyle("-fx-font-size: 16px; -fx-text-fill: #222; -fx-padding: 10 18 10 18;");
                    hbox.getChildren().add(label);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    hbox.setMinHeight(40);
                    hbox.setStyle("-fx-background-color: #e3e9f7; -fx-background-radius: 10;");
                    wrapper.getChildren().add(hbox);
                    wrapper.setPadding(new javafx.geometry.Insets(0, 0, 4, 0)); // 气泡间距由8改为4
                }

                @Override
                protected void updateItem(AiSession session, boolean empty) {
                    super.updateItem(session, empty);
                    if (empty || session == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        label.setText(session.getTitle() + "（" + getRelativeTime(session.getUpdatedAt()) + "）");
                        // 选中时高亮
                        if (isSelected()) {
                            hbox.setStyle("-fx-background-color: #a6b4d6; -fx-background-radius: 10;");
                        } else {
                            hbox.setStyle("-fx-background-color: #e3e9f7; -fx-background-radius: 10;");
                        }
                        setText(null);
                        setGraphic(wrapper);
                    }
                }
            };
            // 右键菜单：删除会话
            MenuItem delSession = new MenuItem("删除会话");
            delSession.setOnAction(e -> onDeleteSession(cell.getItem()));
            ContextMenu menu = new ContextMenu(delSession);
            cell.setContextMenu(menu);
            return cell;
        });

        // 配置消息列表显示
        messageListView.setItems(messageList);
        messageListView.setCellFactory(list -> {
            ListCell<AiMessage> cell = new ListCell<>() {
                private final HBox hbox = new HBox();
                private final Label label = new Label();

                {
                    label.setWrapText(true);
                    label.setMaxWidth(600); // 最大宽度
                    label.setMinHeight(Label.USE_PREF_SIZE); // 让高度自适应内容
                    hbox.getChildren().add(label);
                }

                @Override
                protected void updateItem(AiMessage message, boolean empty) {
                    super.updateItem(message, empty);
                    if (empty || message == null) {
                        setGraphic(null);
                        setText(null);
                        setStyle("");
                    } else {
                        label.setText(message.getContent());
                        if ("user".equals(message.getRole())) {
                            hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                            hbox.setPadding(new javafx.geometry.Insets(4, 0, 4, 40));
                            label.setStyle("-fx-background-color: #e0f7fa; -fx-background-radius: 16; -fx-padding: 10 18 10 18; -fx-font-size: 15; -fx-text-fill: #222;");
                        } else {
                            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            hbox.setPadding(new javafx.geometry.Insets(4, 40, 4, 0));
                            label.setStyle("-fx-background-color: #fffde7; -fx-background-radius: 16; -fx-padding: 10 18 10 18; -fx-font-size: 15; -fx-text-fill: #222;");
                        }
                        setGraphic(hbox);
                        setText(null);
                        setStyle("");
                    }
                }
            };
            // 右键菜单：删除消息
            MenuItem delMsg = new MenuItem("删除消息");
            delMsg.setOnAction(e -> onDeleteMessage(cell.getItem()));
            ContextMenu menu = new ContextMenu(delMsg);
            cell.setContextMenu(menu);
            return cell;
        });

        loadSessions();

        sessionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                currentSessionId = newV.getSessionId();
                sessionTitle.setText(newV.getTitle());
                loadMessages(currentSessionId);
            } else {
                currentSessionId = null;
            }
            updateSendBtnState();
        });
        inputField.textProperty().addListener((obs, oldVal, newVal) -> updateSendBtnState());
        logger.info("AI聊天界面初始化完成");
    }

    private void updateSendBtnState() {
        boolean canSend = !inputField.getText().trim().isEmpty() && sessionListView.getSelectionModel().getSelectedItem() != null;
        sendBtn.setDisable(!canSend);
    }

    @FXML
    private void onInputKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onSend(null);
        }
    }

    @FXML
    private void onSend(ActionEvent event) {
        String msg = inputField.getText().trim();
        AiSession selected = sessionListView.getSelectionModel().getSelectedItem();
        Integer sessionId = (selected != null) ? selected.getSessionId() : null;
        logger.info("onSend: msg='" + msg + "', selectedSessionId=" + sessionId);
        if (msg.isEmpty() || sessionId == null) {
            logger.warning("消息为空或未选择会话，无法发送: msg='" + msg + "', selectedSessionId=" + sessionId);
            return;
        }
        logger.info("发送消息到会话 " + sessionId + ": " + msg);
        sendMessage(sessionId, msg);
    }

    @FXML
    private void onCreateSession(ActionEvent event) {
        logger.info("创建新会话");
        Platform.runLater(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/session"))
                        .header("Authorization", token)
                        .POST(BodyPublishers.noBody())
                        .build();
                HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    logger.info("新会话创建成功");
                    loadSessions();
                } else {
                    logger.warning("创建会话失败: " + resp.body());
                    DashboardController.showAlert("错误", "新建会话失败: " + resp.body(), null, Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "创建会话时发生异常", e);
                DashboardController.showAlert("错误", "网络错误: " + e.getMessage(), null, Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void onBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", backButton);
    }

    // ========== 网络请求与数据处理 ==========
    private void loadSessions() {
        logger.info("开始加载用户 " + username + " 的会话列表");
        Platform.runLater(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/sessions/" + username))
                        .header("Authorization", token)
                        .GET().build();
                HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    ArrayNode arr = (ArrayNode) objectMapper.readTree(resp.body());
                    sessionList.clear();
                    for (JsonNode obj : arr) {
                        AiSession session = objectMapper.treeToValue(obj, AiSession.class);
                        logger.info("加载会话: " + session);
                        sessionList.add(session);
                    }
                    if (!sessionList.isEmpty()) {
                        sessionListView.getSelectionModel().select(0);
                        AiSession selected = sessionListView.getSelectionModel().getSelectedItem();
                        logger.info("默认选择会话: " + selected);
                        if (selected != null) {
                            currentSessionId = selected.getSessionId();
                        } else {
                            currentSessionId = null;
                        }
                    } else {
                        currentSessionId = null;
                    }
                    updateSendBtnState();
                    logger.info("成功加载 " + sessionList.size() + " 个会话");
                } else {
                    logger.warning("获取会话失败: " + resp.body());
                    DashboardController.showAlert("错误", "获取会话失败: " + resp.body(), null, Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "加载会话时发生异常", e);
                DashboardController.showAlert("错误", "网络错误: " + e.getMessage(), null, Alert.AlertType.ERROR);
            }
        });
    }

    private void loadMessages(Integer sessionId) {
        if (sessionId == null) {
            messageList.clear();
            return;
        }

        logger.info("加载会话 " + sessionId + " 的消息");
        Platform.runLater(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/messages/" + sessionId))
                        .header("Authorization", token)
                        .GET().build();
                HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    ArrayNode arr = (ArrayNode) objectMapper.readTree(resp.body());
                    messageList.clear();
                    for (JsonNode obj : arr) {
                        AiMessage message = objectMapper.treeToValue(obj, AiMessage.class);
                        messageList.add(message);
                    }
                    logger.info("成功加载 " + messageList.size() + " 条消息");
                } else {
                    logger.warning("获取消息失败: " + resp.body());
                    DashboardController.showAlert("错误", "获取消息失败: " + resp.body(), null, Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "加载消息时发生异常", e);
                DashboardController.showAlert("错误", "网络错误: " + e.getMessage(), null, Alert.AlertType.ERROR);
            }
        });
    }

    private void sendMessage(Integer sessionId, String msg) {
        if (sessionId == null || msg.isEmpty()) return;

        // 显示用户消息
        AiMessage userMessage = new AiMessage();
        userMessage.setRole("user");
        userMessage.setContent(msg);
        messageList.add(userMessage);
        inputField.clear();

        // 初始化Ai回复消息
        AiMessage aiMessage = new AiMessage();
        aiMessage.setRole("assistant");
        aiMessage.setContent("");
        messageList.add(aiMessage);
        messageListView.scrollTo(messageList.size() - 1);

        String encodedMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        String url = BASE_URL + "/chat/stream?sessionId=" + sessionId + "&userMsg=" + encodedMsg;

        CompletableFuture.runAsync(() -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", token)
                    .GET()
                    .build();

            try {
                client.send(request, HttpResponse.BodyHandlers.ofInputStream())
                        .body()
                        .transferTo(new java.io.OutputStream() {
                            private final StringBuilder buffer = new StringBuilder();

                            @Override
                            public void write(int b) {
                                // 不处理单字节
                            }

                            @Override
                            public void write(byte[] b, int off, int len) {
                                String chunk = new String(b, off, len, StandardCharsets.UTF_8);
                                buffer.append(chunk);
                                while (true) {
                                    int firstIndex = buffer.indexOf("data:");
                                    if (firstIndex == -1) break;
                                    int secondIndex = buffer.substring(firstIndex + 5).indexOf("data:");
                                    if (secondIndex == -1) break;
                                    String data = buffer.substring(firstIndex + 5, firstIndex + 5 + secondIndex).trim();
                                    buffer.delete(0, firstIndex + 5 + secondIndex);
                                    processData(data);
                                }
                            }

                            @Override
                            public void close() {
                                // 流结束时，处理缓冲区剩余的最后一条 data
                                if (!buffer.isEmpty()) {
                                    int index = buffer.indexOf("data: ");
                                    if (index != -1) {
                                        processData(buffer.substring(index));
                                        buffer.setLength(0);
                                    }
                                }
                            }

                            private void processData(String data) {
                                AiMessage aiMessage = messageList.get(messageList.size() - 1);
                                aiMessage.setContent(aiMessage.getContent() + data);
                                Platform.runLater(() -> {
                                    messageListView.refresh();
                                });
                            }
                        });

            } catch (Exception e) {
                logger.log(Level.SEVERE, "流式接收失败", e);
                Platform.runLater(() -> DashboardController.showAlert(
                        "错误", "接收 AI 消息失败: " + e.getMessage(), null, Alert.AlertType.ERROR));
            }
        });
    }

    // 删除会话
    private void onDeleteSession(AiSession session) {
        if (session == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除该会话及其所有消息吗？", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("删除会话");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                CompletableFuture.runAsync(() -> {
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/session/" + session.getSessionId()))
                                .header("Authorization", token)
                                .DELETE().build();
                        HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                        if (resp.statusCode() == 200) {
                            Platform.runLater(this::loadSessions);
                        } else {
                            Platform.runLater(() -> DashboardController.showAlert("错误", "删除会话失败: " + resp.body(), null, Alert.AlertType.ERROR));
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "删除会话异常", e);
                        Platform.runLater(() -> DashboardController.showAlert("错误", "网络错误: " + e.getMessage(), null, Alert.AlertType.ERROR));
                    }
                });
            }
        });
    }

    // 删除消息
    private void onDeleteMessage(AiMessage msg) {
        if (msg == null || msg.getMsgId() == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除该消息吗？", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("删除消息");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                CompletableFuture.runAsync(() -> {
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/message/" + msg.getMsgId()))
                                .header("Authorization", token)
                                .DELETE().build();
                        HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                        if (resp.statusCode() == 200) {
                            Platform.runLater(() -> loadMessages(currentSessionId));
                        } else {
                            Platform.runLater(() -> DashboardController.showAlert("错误", "删除消息失败: " + resp.body(), null, Alert.AlertType.ERROR));
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "删除消息异常", e);
                        Platform.runLater(() -> DashboardController.showAlert("错误", "网络错误: " + e.getMessage(), null, Alert.AlertType.ERROR));
                    }
                });
            }
        });
    }

    /**
     * 将时间字符串（如 2025-09-18 18:22:32）转为“xx天前/xx小时前/xx分钟前/刚刚”
     */
    private String getRelativeTime(String updatedAt) {
        if (updatedAt == null || updatedAt.isEmpty()) return "未知";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.parse(updatedAt, formatter);
            Duration duration = Duration.between(time, LocalDateTime.now());
            long days = duration.toDays();
            long hours = duration.toHours();
            long minutes = duration.toMinutes();
            if (days > 0) return days + "天前";
            if (hours > 0) return hours + "小时前";
            if (minutes > 0) return minutes + "分钟前";
            return "刚刚";
        } catch (Exception e) {
            return updatedAt;
        }
    }

}
