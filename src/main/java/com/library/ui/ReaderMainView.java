package com.library.ui;

import javafx.application.Platform;
import com.library.model.User;
import com.library.ui.panels.reader.*;
import com.library.util.Constants;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ReaderMainView extends BorderPane {
    private User currentUser;
    private StackPane contentArea;
    private VBox menuBox;
    private Button activeButton;
    private Runnable onLogout;

    public ReaderMainView(User user) {
        this.currentUser = user;
        buildUI();
        showCatalog();
    }

    private void buildUI() {
        // Sidebar
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);
        sidebar.setMinWidth(260);

        // Logo
        VBox logoBox = new VBox(3);
        logoBox.getStyleClass().add("sidebar-logo");
        
        ImageView logoView = null;
        try {
            logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
            logoView.setFitHeight(30);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {}
        
        Label logoLabel = new Label((logoView == null ? "\uD83D\uDCDA " : "") + Constants.APP_TITLE);
        logoLabel.getStyleClass().add("label");
        
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        if (logoView != null) titleRow.getChildren().add(logoView);
        titleRow.getChildren().add(logoLabel);
        
        Label modeLabel = new Label("📖 Chế độ Người đọc");
        modeLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 12px; -fx-font-weight: bold;");
        logoBox.getChildren().addAll(titleRow, modeLabel);

        // Menu items
        menuBox = new VBox(4);
        menuBox.setPadding(new Insets(15, 12, 15, 12));

        Button catalogBtn = createMenuButton("📚  Kho sách", e -> showCatalog());
        Button myBorrowsBtn = createMenuButton("⏳  Sách đang mượn", e -> showMyBorrows());
        Button cartBtn = createMenuButton("🛒  Giỏ hàng", e -> showCart());
        Button myReservationsBtn = createMenuButton("📋  Sách đã đặt", e -> showMyReservations());
        Button readOnlineBtn = createMenuButton("📖  Đọc Online", e -> showReadOnline());
        Button favoritesBtn = createMenuButton("💖  Sách yêu thích", e -> showFavorites());
        Button chatBtn = createMenuButton("💬  Tin nhắn", e -> showChat());

        menuBox.getChildren().addAll(catalogBtn, myBorrowsBtn, cartBtn, myReservationsBtn, readOnlineBtn, favoritesBtn, chatBtn);

        // Badge for chat
        com.library.service.ChatService.getInstance().addNotificationListener(count -> {
            Platform.runLater(() -> {
                if (count > 0) {
                    chatBtn.setText("\uD83D\uDCAC  Tin nhắn (" + count + ")");
                    chatBtn.setStyle("-fx-text-fill: #f38ba8;");
                } else {
                    chatBtn.setText("\uD83D\uDCAC  Tin nhắn");
                    chatBtn.setStyle("");
                }
            });
        });
        com.library.service.ChatService.getInstance().start(currentUser);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User info
        VBox userBox = new VBox(6);
        userBox.getStyleClass().add("sidebar-user-info");
        userBox.setPadding(new Insets(10));
        VBox.setMargin(userBox, new Insets(0, 12, 12, 12));

        Label userNameLabel = new Label(currentUser.getFullName());
        userNameLabel.getStyleClass().add("sidebar-user-name");
        Label userRoleLabel = new Label("Người đọc");
        userRoleLabel.getStyleClass().add("sidebar-user-role");

        Button settingsBtn = new Button("⚙️ Cài đặt");
        settingsBtn.getStyleClass().add("sidebar-btn");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setStyle("-fx-text-fill: #a6adc8; -fx-alignment: center-left; -fx-padding: 8 12;");
        settingsBtn.setOnAction(e -> showProfile());

        Button logoutBtn = new Button("\uD83D\uDEAA Đăng xuất");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        userBox.getChildren().addAll(userNameLabel, userRoleLabel, settingsBtn, logoutBtn);

        sidebar.getChildren().addAll(logoBox, menuBox, spacer, userBox);

        // Content area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        setLeft(sidebar);
        setCenter(contentArea);
    }

    private Button createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            setActiveButton(btn);
            action.handle(e);
        });
        return btn;
    }

    private void setActiveButton(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
        }
        activeButton = btn;
        btn.getStyleClass().add("sidebar-btn-active");
    }

    private void setContent(javafx.scene.Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    private void showCatalog() {
        setActiveButton((Button) menuBox.getChildren().get(0));
        setContent(new ReaderCatalogPanel(currentUser));
    }
    
    private void showCart() {
        setContent(new ReaderCartPanel(currentUser));
    }

    private void showMyBorrows() {
        setContent(new ReaderBorrowPanel(currentUser));
    }

    private void showMyReservations() {
        setContent(new ReaderReservationsPanel(currentUser));
    }

    private void showReadOnline() {
        setContent(new ReaderReadOnlinePanel(currentUser));
    }

    private void showFavorites() {
        setContent(new ReaderFavoritesPanel(currentUser));
    }

    private void showProfile() {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
            activeButton = null;
        }
        setContent(new ReaderProfilePanel(currentUser, () -> {
            // Update username label when profile name changes
            VBox userBox = (VBox) ((VBox) getLeft()).getChildren().get(3);
            Label nameLbl = (Label) userBox.getChildren().get(0);
            nameLbl.setText(currentUser.getFullName());
        }));
    }

    private void showChat() {
        setContent(new com.library.ui.panels.chat.ChatMainPanel(currentUser));
    }

    public void setOnLogout(Runnable action) { this.onLogout = action; }
    public User getCurrentUser() { return currentUser; }
}
