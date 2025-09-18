package seu.virtualcampus.ui.aichat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seu.virtualcampus.domain.AiMessage;
import seu.virtualcampus.domain.AiSession;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AiChatController implements Initializable {
    private static final Logger logger = Logger.getLogger(AiChatController.class.getName());
    private final String BASE_URL = "http://" + MainApp.host + "/api/ai-chat";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<AiSession> sessionList = FXCollections.observableArrayList();
    private final ObservableList<AiMessage> messageList = FXCollections.observableArrayList();
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
        sessionListView.setCellFactory(list -> new ListCell<AiSession>() {
            @Override
            protected void updateItem(AiSession session, boolean empty) {
                super.updateItem(session, empty);
                if (empty || session == null) {
                    setText(null);
                } else {
                    setText(session.getTitle() + " (最后访问: " +
                            session.getUpdatedAt() + ")");
                }
            }
        });

        // 配置消息列表显示
        messageListView.setItems(messageList);
        messageListView.setCellFactory(list -> new ListCell<AiMessage>() {
            @Override
            protected void updateItem(AiMessage message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    setText(message.getRole() + ": " + message.getContent());
                }
            }
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
        sendBtn.setOnAction(this::onSend);
        createSessionBtn.setOnAction(this::onCreateSession);
        inputField.setOnKeyPressed(this::onInputKey);
        inputField.textProperty().addListener((obs, oldVal, newVal) -> updateSendBtnState());
        logger.info("AI聊天界面初始化完成");
    }

    private void updateSendBtnState() {
        boolean canSend = !inputField.getText().trim().isEmpty() && sessionListView.getSelectionModel().getSelectedItem() != null;
        sendBtn.setDisable(!canSend);
    }

    private void onInputKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onSend(null);
        }
    }

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
        logger.info("向会话 " + sessionId + " 发送消息");
        Platform.runLater(() -> {
            try {
                ObjectNode payload = objectMapper.createObjectNode();
                payload.put("sessionId", sessionId);
                payload.put("userMsg", msg);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/chat"))
                        .header("Authorization", token)
                        .header("Content-Type", "application/json")
                        .POST(BodyPublishers.ofString(payload.toString()))
                        .build();
                HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    logger.info("消息发送成功");
                    inputField.clear();
                    loadMessages(sessionId);
                } else {
                    logger.warning("发送消息失败: " + resp.body());
                    DashboardController.showAlert("错误", "发送失败: " + resp.body(), null, Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "发送消息时发生异常", e);
                DashboardController.showAlert("错误", "网络错误: " + e.getMessage(), null, Alert.AlertType.ERROR);
            }
        });
    }
}
