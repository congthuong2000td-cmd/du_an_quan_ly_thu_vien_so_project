package com.library.ui;

import javafx.application.Platform;
import com.library.model.User;
import com.library.ui.panels.*;
import com.library.ui.panels.admin.ReservationAdminPanel;
import com.library.util.Constants;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class MainView extends BorderPane {
    private User currentUser;
    private StackPane contentArea;
    private VBox menuBox;
    private Button activeButton;
    private Label userNameLabel;
    private Label userRoleLabel;
    private Runnable onLogout;
    private DashboardPanel dashboardPanel;
    private BookPanel bookPanel;
    private ReaderPanel readerPanel;
    private BorrowPanel borrowPanel;
    private StatisticsPanel statisticsPanel;
    private SettingsPanel settingsPanel;
    private UserManagementPanel userManagementPanel;
    private ReservationAdminPanel reservationPanel;

    public MainView(User user) {
        this.currentUser = user;
        buildUI();
        showDashboard();
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
        Label versionLabel = new Label("Phiên bản " + Constants.APP_VERSION);
        versionLabel.getStyleClass().addAll("label");
        versionLabel.setStyle("-fx-font-size: 11px; -fx-opacity: 0.7;");
        logoBox.getChildren().addAll(titleRow, versionLabel);

        // Menu items
        menuBox = new VBox(4);
        menuBox.setPadding(new Insets(15, 12, 15, 12));

        Button dashBtn = createMenuButton("\uD83D\uDCCA  Tổng quan", e -> showDashboard());
        Button bookBtn = createMenuButton("\uD83D\uDCDA  Quản lý Sách", e -> showBooks());
        Button catBtn = createMenuButton("🏷️  Quản lý Thể loại", e -> showCategories());
        Button readerBtn = createMenuButton("\uD83D\uDC65  Quản lý Độc giả", e -> showReaders());
        Button borrowBtn = createMenuButton("\uD83D\uDD04  Mượn / Trả sách", e -> showBorrow());
        Button reserveBtn = createMenuButton("\uD83D\uDCCB  Duyệt đặt sách", e -> showReservations());
        Button statsBtn = createMenuButton("\uD83D\uDCC8  Thống kê", e -> showStatistics());
        Button chatBtn = createMenuButton("\uD83D\uDCAC  Tin nhắn", e -> showChat());
        
        menuBox.getChildren().addAll(dashBtn, bookBtn, catBtn, readerBtn, borrowBtn, reserveBtn, statsBtn, chatBtn);

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

        // User management (Admin only)
        if (currentUser.isAdmin()) {
            Button usersBtn = createMenuButton("👤  Quản lý Tài khoản", e -> showUsers());
            menuBox.getChildren().add(usersBtn);

            // Show badge if pending users
            com.library.dao.UserDAO tempDAO = new com.library.dao.UserDAO();
            int pending = tempDAO.getPendingCount();
            if (pending > 0) {
                usersBtn.setText("👤  Quản lý Tài khoản (" + pending + ")");
                usersBtn.setStyle("-fx-text-fill: #fab387;");
            }
        }

        Button settingsBtn = createMenuButton("\u2699\uFE0F  Cài đặt", e -> showSettings());
        menuBox.getChildren().add(settingsBtn);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User info
        VBox userBox = new VBox(6);
        userBox.getStyleClass().add("sidebar-user-info");
        userBox.setPadding(new Insets(10));
        VBox.setMargin(userBox, new Insets(0, 12, 12, 12));

        userNameLabel = new Label(currentUser.getFullName());
        userNameLabel.getStyleClass().add("sidebar-user-name");
        userRoleLabel = new Label(currentUser.isAdmin() ? "Quản trị viên" : "Thủ thư");
        userRoleLabel.getStyleClass().add("sidebar-user-role");

        Button logoutBtn = new Button("\uD83D\uDEAA Đăng xuất");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        userBox.getChildren().addAll(userNameLabel, userRoleLabel, logoutBtn);

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

    private void showDashboard() {
        setActiveButton((Button) menuBox.getChildren().get(0));
        dashboardPanel = new DashboardPanel();
        setContent(dashboardPanel);
    }

    private void showBooks() {
        setContent(new BookPanel());
    }

    private void showCategories() {
        setContent(new com.library.ui.panels.admin.CategoryPanel());
    }

    private void showReaders() {
        readerPanel = new ReaderPanel();
        setContent(readerPanel);
    }

    private void showBorrow() {
        borrowPanel = new BorrowPanel();
        setContent(borrowPanel);
    }

    private void showReservations() {
        reservationPanel = new ReservationAdminPanel();
        setContent(reservationPanel);
    }

    private void showStatistics() {
        statisticsPanel = new StatisticsPanel();
        setContent(statisticsPanel);
    }

    private void showSettings() {
        settingsPanel = new SettingsPanel(this);
        setContent(settingsPanel);
    }

    private void showUsers() {
        userManagementPanel = new UserManagementPanel();
        setContent(userManagementPanel);
    }

    private void showChat() {
        setContent(new com.library.ui.panels.chat.ChatMainPanel(currentUser));
    }

    public void setOnLogout(Runnable action) { this.onLogout = action; }
    public User getCurrentUser() { return currentUser; }
}
