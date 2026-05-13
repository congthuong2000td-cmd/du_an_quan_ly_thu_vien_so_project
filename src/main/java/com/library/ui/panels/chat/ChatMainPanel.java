package com.library.ui.panels.chat;

import com.library.dao.ConversationDAO;
import com.library.dao.UserDAO;
import com.library.model.Conversation;
import com.library.model.Message;
import com.library.model.User;
import com.library.service.ChatService;
import com.library.util.FileHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ChatMainPanel extends HBox {
    private final User currentUser;
    private final ChatService chatService = ChatService.getInstance();
    private final UserDAO userDAO = new UserDAO();
    private final ConversationDAO conversationDAO = new ConversationDAO();

    private ListView<Conversation> convListView;
    private VBox messageArea;
    private ScrollPane chatScrollPane;
    private TextField messageInput;
    private Label activeChatName;
    private Conversation activeConversation;

    public ChatMainPanel(User user) {
        this.currentUser = user;
        getStyleClass().add("chat-main-container");
        
        buildUI();
        loadConversations();
        
        chatService.addMessageListener(this::handleNewMessage);
    }

    private void buildUI() {
        // Sidebar
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(300);
        sidebar.getStyleClass().add("chat-sidebar");

        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(15));
        TextField searchField = new TextField();
        searchField.setPromptText("Tìm người dùng...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setOnAction(e -> searchAndStartChat(searchField.getText()));
        searchBox.getChildren().add(searchField);

        convListView = new ListView<>();
        convListView.setCellFactory(lv -> new ConversationCell(currentUser.getId()));
        VBox.setVgrow(convListView, Priority.ALWAYS);
        convListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) openConversation(newVal);
        });

        sidebar.getChildren().addAll(searchBox, convListView);

        // Chat Area
        VBox chatArea = new VBox(0);
        HBox.setHgrow(chatArea, Priority.ALWAYS);
        chatArea.getStyleClass().add("chat-area");

        // Chat Header
        HBox chatHeader = new HBox(15);
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(15, 20, 15, 20));
        chatHeader.getStyleClass().add("chat-header");

        activeChatName = new Label("Chọn một cuộc trò chuyện");
        activeChatName.getStyleClass().add("chat-active-name");
        chatHeader.getChildren().add(activeChatName);

        // Message List
        messageArea = new VBox(10);
        messageArea.setPadding(new Insets(20));
        chatScrollPane = new ScrollPane(messageArea);
        chatScrollPane.setFitToWidth(true);
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);
        chatScrollPane.getStyleClass().add("chat-scroll-pane");

        // Input Area
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15));
        inputArea.setAlignment(Pos.CENTER_LEFT);
        inputArea.getStyleClass().add("chat-input-area");

        Button attachBtn = new Button("📎");
        attachBtn.getStyleClass().add("chat-icon-btn");
        attachBtn.setOnAction(e -> handleAttachFile());

        messageInput = new TextField();
        messageInput.setPromptText("Nhập tin nhắn...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        messageInput.setOnAction(e -> handleSendMessage());

        Button sendBtn = new Button("🚀");
        sendBtn.getStyleClass().add("chat-send-btn");
        sendBtn.setOnAction(e -> handleSendMessage());

        inputArea.getChildren().addAll(attachBtn, messageInput, sendBtn);

        chatArea.getChildren().addAll(chatHeader, chatScrollPane, inputArea);

        getChildren().addAll(sidebar, chatArea);
    }

    private void loadConversations() {
        List<Conversation> conversations = chatService.getConversations();
        convListView.setItems(FXCollections.observableArrayList(conversations));
    }

    private void openConversation(Conversation conversation) {
        this.activeConversation = conversation;
        activeChatName.setText(conversation.getDisplayName(currentUser.getId()));
        
        chatService.markAsRead(conversation.getId());
        
        messageArea.getChildren().clear();
        List<Message> history = chatService.getChatHistory(conversation.getId());
        for (Message m : history) {
            messageArea.getChildren().add(new MessageBubble(m, currentUser.getId()));
        }
        
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        
        // Refresh sidebar to update unread counts
        loadConversations();
    }

    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || activeConversation == null) return;

        Message m = new Message(activeConversation.getId(), currentUser.getId(), text, "TEXT");
        m.setSenderName(currentUser.getFullName());
        
        chatService.sendMessage(m);
        messageInput.clear();
        
        // UI is updated via listener
    }

    private void handleAttachFile() {
        if (activeConversation == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp gửi");
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        
        if (file != null) {
            String savedPath = FileHelper.saveChatFile(file);
            if (savedPath != null) {
                Message m = new Message(activeConversation.getId(), currentUser.getId(), "[Tệp đính kèm: " + file.getName() + "]", "FILE");
                m.setFilePath(savedPath);
                m.setSenderName(currentUser.getFullName());
                chatService.sendMessage(m);
            }
        }
    }

    private void handleNewMessage(Message m) {
        if (activeConversation != null && m.getConversationId() == activeConversation.getId()) {
            Platform.runLater(() -> {
                messageArea.getChildren().add(new MessageBubble(m, currentUser.getId()));
                chatScrollPane.setVvalue(1.0);
                chatService.markAsRead(activeConversation.getId());
            });
        }
        Platform.runLater(this::loadConversations);
    }

    private void searchAndStartChat(String query) {
        if (query.isEmpty()) return;
        
        List<User> users = userDAO.searchUsers(query, currentUser.getId());
        if (users.isEmpty()) {
            showAlert("Không tìm thấy người dùng nào.");
            return;
        }

        ChoiceDialog<User> dialog = new ChoiceDialog<>(users.get(0), users);
        dialog.setTitle("Bắt đầu trò chuyện");
        dialog.setHeaderText("Chọn người dùng để nhắn tin:");
        dialog.setContentText("Người dùng:");

        dialog.showAndWait().ifPresent(targetUser -> {
            // Check if direct conversation exists
            int existingId = conversationDAO.findDirectConversation(currentUser.getId(), targetUser.getId());
            if (existingId != -1) {
                // Find in list and select
                for (Conversation c : convListView.getItems()) {
                    if (c.getId() == existingId) {
                        convListView.getSelectionModel().select(c);
                        return;
                    }
                }
            } else {
                // Create new
                int newId = conversationDAO.createConversation(null, "DIRECT", 
                    List.of(currentUser.getId(), targetUser.getId()));
                if (newId != -1) {
                    loadConversations();
                    for (Conversation c : convListView.getItems()) {
                        if (c.getId() == newId) {
                            convListView.getSelectionModel().select(c);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
