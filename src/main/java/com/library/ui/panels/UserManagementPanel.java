package com.library.ui.panels;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.util.Constants;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class UserManagementPanel extends VBox {
    private final UserDAO userDAO = new UserDAO();
    private TableView<User> table;
    private ObservableList<User> userData;
    private Label pendingCountLabel;
    private ComboBox<String> filterCombo;

    public UserManagementPanel() {
        setSpacing(0);
        setPadding(new Insets(0));
        buildUI();
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        // Header
        HBox header = new HBox(15);
        header.getStyleClass().add("panel-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));

        Label titleLabel = new Label("👥 Quản lý Tài khoản");
        titleLabel.getStyleClass().add("panel-title");

        pendingCountLabel = new Label();
        pendingCountLabel.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e; -fx-padding: 4 12; " +
                "-fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter
        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("Tất cả", "Chờ duyệt", "Đã duyệt");
        filterCombo.setValue("Tất cả");
        filterCombo.setOnAction(e -> loadData());

        Button refreshBtn = new Button("🔄 Làm mới");
        refreshBtn.getStyleClass().add("action-btn");
        refreshBtn.setOnAction(e -> loadData());

        header.getChildren().addAll(titleLabel, pendingCountLabel, spacer, filterCombo, refreshBtn);

        // Table
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60); idCol.setMinWidth(60);

        TableColumn<User, String> usernameCol = new TableColumn<>("Tên đăng nhập");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setMinWidth(120);

        TableColumn<User, String> fullNameCol = new TableColumn<>("Họ và tên");
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        fullNameCol.setMinWidth(180);

        TableColumn<User, String> roleCol = new TableColumn<>("Vai trò");
        roleCol.setCellValueFactory(data -> {
            String role = data.getValue().getRole();
            String display = switch (role) {
                case "ADMIN" -> "Quản trị viên";
                case "LIBRARIAN" -> "Thủ thư";
                case "READER" -> "Độc giả";
                default -> role;
            };
            return new SimpleStringProperty(display);
        });
        roleCol.setMinWidth(110);
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Quản trị viên" -> setStyle("-fx-text-fill: #cba6f7; -fx-font-weight: bold;");
                    case "Thủ thư" -> setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold;");
                    case "Độc giả" -> setStyle("-fx-text-fill: #94e2d5; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        TableColumn<User, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusText()));
        statusCol.setMinWidth(120);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Đã duyệt".equals(item)) {
                        setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #fab387; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<User, Void> actionCol = new TableColumn<>("Hành động");
        actionCol.setMinWidth(280);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("✅ Duyệt");
            private final Button rejectBtn = new Button("❌ Từ chối");
            private final Button deactivateBtn = new Button("🔒 Khóa");
            private final Button activateBtn = new Button("🔓 Mở khóa");
            private final Button deleteBtn = new Button("🗑 Xóa");
            private final HBox box = new HBox(5);

            {
                approveBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                deactivateBtn.setStyle("-fx-background-color: #fab387; -fx-text-fill: #1e1e2e; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                activateBtn.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #f38ba8; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");

                box.setAlignment(Pos.CENTER);

                approveBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleApprove(user);
                });
                rejectBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleReject(user);
                });
                deactivateBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeactivate(user);
                });
                activateBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleApprove(user);
                });
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDelete(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    box.getChildren().clear();
                    // Don't show actions for admin accounts
                    if (user.isAdmin()) {
                        setGraphic(null);
                        return;
                    }
                    if (!user.isActive()) {
                        box.getChildren().addAll(approveBtn, rejectBtn);
                    } else {
                        box.getChildren().addAll(deactivateBtn, deleteBtn);
                    }
                    setGraphic(box);
                }
            }
        });

        // Role change column
        TableColumn<User, Void> changeRoleCol = new TableColumn<>("Đổi vai trò");
        changeRoleCol.setMinWidth(150);
        changeRoleCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> roleCombo = new ComboBox<>();
            {
                roleCombo.getItems().addAll("Quản trị viên", "Thủ thư", "Độc giả");
                roleCombo.setStyle("-fx-font-size: 11px; -fx-background-color: #313244; -fx-text-fill: #cdd6f4;");
                roleCombo.setMaxWidth(Double.MAX_VALUE);
                roleCombo.setOnAction(e -> {
                    if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) return;
                    User user = getTableView().getItems().get(getIndex());
                    String selected = roleCombo.getValue();
                    String newRole = switch (selected) {
                        case "Quản trị viên" -> Constants.ROLE_ADMIN;
                        case "Độc giả" -> Constants.ROLE_READER;
                        default -> Constants.ROLE_LIBRARIAN;
                    };
                    if (!newRole.equals(user.getRole())) {
                        handleChangeRole(user, newRole, selected);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.isAdmin()) {
                        setGraphic(null);
                    } else {
                        String currentDisplay = switch (user.getRole()) {
                            case "ADMIN" -> "Quản trị viên";
                            case "READER" -> "Độc giả";
                            default -> "Thủ thư";
                        };
                        roleCombo.setValue(currentDisplay);
                        setGraphic(roleCombo);
                    }
                }
            }
        });

        table.getColumns().addAll(idCol, usernameCol, fullNameCol, roleCol, changeRoleCol, statusCol, actionCol);

        // Info bar at bottom
        HBox infoBar = new HBox(15);
        infoBar.setPadding(new Insets(12, 25, 12, 25));
        infoBar.setAlignment(Pos.CENTER_LEFT);
        infoBar.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 1 0 0 0;");

        Label infoIcon = new Label("ℹ️");
        Label infoText = new Label("Tài khoản mới đăng ký cần được Admin phê duyệt trước khi có thể đăng nhập.");
        infoText.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12px;");
        infoBar.getChildren().addAll(infoIcon, infoText);

        getChildren().addAll(header, table, infoBar);
    }

    private void loadData() {
        String filter = filterCombo.getValue();
        List<User> users;

        if ("Chờ duyệt".equals(filter)) {
            users = userDAO.getPendingUsers();
        } else if ("Đã duyệt".equals(filter)) {
            users = userDAO.getAll().stream().filter(User::isActive).toList();
        } else {
            users = userDAO.getAll();
        }

        userData = FXCollections.observableArrayList(users);
        table.setItems(userData);

        int pendingCount = userDAO.getPendingCount();
        if (pendingCount > 0) {
            pendingCountLabel.setText(pendingCount + " chờ duyệt");
            pendingCountLabel.setVisible(true);
            pendingCountLabel.setManaged(true);
        } else {
            pendingCountLabel.setVisible(false);
            pendingCountLabel.setManaged(false);
        }
    }

    private void handleApprove(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận phê duyệt");
        confirm.setHeaderText("Phê duyệt tài khoản: " + user.getFullName());
        confirm.setContentText("Tài khoản \"" + user.getUsername() + "\" sẽ được kích hoạt và có thể đăng nhập vào hệ thống.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.activateUser(user.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "✅ Đã phê duyệt tài khoản \"" + user.getUsername() + "\"!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể phê duyệt tài khoản!");
            }
        }
    }

    private void handleReject(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận từ chối");
        confirm.setHeaderText("Từ chối tài khoản: " + user.getFullName());
        confirm.setContentText("Tài khoản \"" + user.getUsername() + "\" sẽ bị xóa vĩnh viễn. Bạn có chắc chắn?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deleteUser(user.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "❌ Đã từ chối và xóa tài khoản \"" + user.getUsername() + "\"!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản!");
            }
        }
    }

    private void handleDeactivate(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận khóa tài khoản");
        confirm.setHeaderText("Khóa tài khoản: " + user.getFullName());
        confirm.setContentText("Tài khoản \"" + user.getUsername() + "\" sẽ bị khóa và không thể đăng nhập.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deactivateUser(user.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "🔒 Đã khóa tài khoản \"" + user.getUsername() + "\"!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khóa tài khoản!");
            }
        }
    }

    private void handleDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa tài khoản: " + user.getFullName());
        confirm.setContentText("Tài khoản \"" + user.getUsername() + "\" sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deleteUser(user.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "🗑 Đã xóa tài khoản \"" + user.getUsername() + "\"!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản!");
            }
        }
    }

    private void handleChangeRole(User user, String newRole, String displayName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đổi vai trò");
        confirm.setHeaderText("Thay đổi vai trò: " + user.getFullName());
        confirm.setContentText("Bạn có chắc muốn đổi vai trò của \"" + user.getUsername() + "\" thành \"" + displayName + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.updateRole(user.getId(), newRole)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "✅ Đã đổi vai trò của \"" + user.getUsername() + "\" thành \"" + displayName + "\"!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đổi vai trò!");
            }
        } else {
            loadData(); // Reset combo value
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
