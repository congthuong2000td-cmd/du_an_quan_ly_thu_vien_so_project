package com.library.ui.panels.chat;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.library.dao.ConversationDAO;
import com.library.dao.UserDAO;
import com.library.model.Conversation;
import com.library.model.Message;
import com.library.model.User;
import com.library.service.ChatService;
import com.library.util.FileHelper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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
    private final Set<Integer> displayedMessageIds = new HashSet<>();

    private boolean isUpdatingSelection = false;

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
        
        Button newChatBtn = new Button("➕");
        newChatBtn.getStyleClass().add("chat-icon-btn");
        newChatBtn.setOnAction(e -> searchAndStartChat(""));

        searchBox.getChildren().addAll(searchField, newChatBtn);

        convListView = new ListView<>();
        convListView.setCellFactory(lv -> new ConversationCell(currentUser.getId()));
        VBox.setVgrow(convListView, Priority.ALWAYS);
        convListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isUpdatingSelection)
                openConversation(newVal);
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

        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("chat-send-btn");
        sendBtn.setOnAction(e -> handleSendMessage());

        inputArea.getChildren().addAll(attachBtn, messageInput, sendBtn);

        chatArea.getChildren().addAll(chatHeader, chatScrollPane, inputArea);

        getChildren().addAll(sidebar, chatArea);
    }

    private void loadConversations() {
        int selectedId = activeConversation != null ? activeConversation.getId() : -1;
        List<Conversation> conversations = chatService.getConversations();
        
        isUpdatingSelection = true;
        convListView.setItems(FXCollections.observableArrayList(conversations));
        
        if (selectedId != -1) {
            for (Conversation c : convListView.getItems()) {
                if (c.getId() == selectedId) {
                    convListView.getSelectionModel().select(c);
                    break;
                }
            }
        }
        isUpdatingSelection = false;
    }

    private void openConversation(Conversation conversation) {
        this.activeConversation = conversation;
        activeChatName.setText(conversation.getDisplayName(currentUser.getId()));

        chatService.markAsRead(conversation.getId());

        messageArea.getChildren().clear();
        displayedMessageIds.clear();
        
        // Load messages in background thread to prevent UI lag
        new Thread(() -> {
            // Load only recent messages (last 50) for faster display - much better performance!
            List<Message> history = chatService.getRecentMessages(conversation.getId());
            Platform.runLater(() -> {
                for (Message m : history) {
                    if (!displayedMessageIds.contains(m.getId())) {
                        messageArea.getChildren().add(new MessageBubble(m, currentUser.getId()));
                        displayedMessageIds.add(m.getId());
                    }
                }
                chatScrollPane.setVvalue(1.0);
            });
        }).start();

        // Refresh sidebar to update unread counts
        loadConversations();
    }

    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || activeConversation == null)
            return;

        Message m = new Message(activeConversation.getId(), currentUser.getId(), text, "TEXT");
        m.setSenderName(currentUser.getFullName());

        chatService.sendMessage(m);
        messageInput.clear();

        // UI is updated via listener
    }

    private void handleAttachFile() {
        if (activeConversation == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp gửi");
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            String savedPath = FileHelper.saveChatFile(file);
            if (savedPath != null) {
                Message m = new Message(activeConversation.getId(), currentUser.getId(),
                        "[Tệp đính kèm: " + file.getName() + "]", "FILE");
                m.setFilePath(savedPath);
                m.setSenderName(currentUser.getFullName());
                chatService.sendMessage(m);
            }
        }
    }

    private void handleNewMessage(Message m) {
        if (activeConversation != null && m.getConversationId() == activeConversation.getId()) {
            Platform.runLater(() -> {
                if (!displayedMessageIds.contains(m.getId())) {
                    messageArea.getChildren().add(new MessageBubble(m, currentUser.getId()));
                    displayedMessageIds.add(m.getId());
                }
                chatScrollPane.setVvalue(1.0);
                chatService.markAsRead(activeConversation.getId());
            });
        }
        Platform.runLater(this::loadConversations);
    }

    private void searchAndStartChat(String query) {
        List<User> users = userDAO.searchUsers(query, currentUser.getId());
        
        if (users.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy người dùng nào phù hợp hoặc người dùng chưa kích hoạt tài khoản.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<User> dialog = new ChoiceDialog<>(users.get(0), users);
        dialog.setTitle("Bắt đầu trò chuyện");
        dialog.setHeaderText("Chọn người dùng để nhắn tin:");
        dialog.setContentText("Người dùng:");
        
        // Custom cell factory for ChoiceDialog to show full name
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return dialog.getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(targetUser -> {
            // Check if direct conversation exists
            int existingId = conversationDAO.findDirectConversation(currentUser.getId(), targetUser.getId());
            
            if (existingId != -1) {
                loadConversations();
                for (Conversation c : convListView.getItems()) {
                    if (c.getId() == existingId) {
                        convListView.getSelectionModel().select(c);
                        return;
                    }
                }
            } else {
                // Create new conversation
                int newId = conversationDAO.createConversation(
                    targetUser.getFullName(), 
                    "DIRECT", 
                    java.util.Arrays.asList(currentUser.getId(), targetUser.getId())
                );
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
