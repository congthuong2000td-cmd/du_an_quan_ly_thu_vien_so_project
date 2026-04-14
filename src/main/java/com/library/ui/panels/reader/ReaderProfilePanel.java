package com.library.ui.panels.reader;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.util.ValidationUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReaderProfilePanel extends VBox {
    private final UserDAO userDAO = new UserDAO();
    private final User currentUser;
    private final Runnable onProfileUpdated;

    public ReaderProfilePanel(User user, Runnable onProfileUpdated) {
        this.currentUser = user;
        this.onProfileUpdated = onProfileUpdated;
        setSpacing(20);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_CENTER);
        buildUI();
    }

    private void buildUI() {
        Label titleLabel = new Label("⚙️ Cài Đặt Cá Nhân");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #cdd6f4;");

        VBox formCard = new VBox(15);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(500);
        formCard.setPadding(new Insets(25));

        // Tên hiển thị
        Label nameLabel = new Label("Họ và Tên:");
        nameLabel.setStyle("-fx-text-fill: #cdd6f4;");
        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Nhập họ và tên mới");

        Button updateNameBtn = new Button("Cập nhật Tên");
        updateNameBtn.getStyleClass().add("action-btn");
        updateNameBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên không được để trống!");
                return;
            }
            if (userDAO.updateFullName(currentUser.getId(), newName)) {
                currentUser.setFullName(newName);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi tên thành công!");
                if (onProfileUpdated != null) onProfileUpdated.run();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật tên.");
            }
        });
        HBox nameRow = new HBox(10, nameField, updateNameBtn);
        HBox.setHgrow(nameField, Priority.ALWAYS);

        // Đổi mật khẩu
        Label passTitleLabel = new Label("Đổi Mật Khẩu");
        passTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f9e2af; -fx-padding: 20 0 5 0;");

        Label oldPassLabel = new Label("Mật khẩu cũ:");
        oldPassLabel.setStyle("-fx-text-fill: #cdd6f4;");
        PasswordField oldPassField = new PasswordField();

        Label newPassLabel = new Label("Mật khẩu mới:");
        newPassLabel.setStyle("-fx-text-fill: #cdd6f4;");
        PasswordField newPassField = new PasswordField();

        Label confirmPassLabel = new Label("Xác nhận MK mới:");
        confirmPassLabel.setStyle("-fx-text-fill: #cdd6f4;");
        PasswordField confirmPassField = new PasswordField();

        Button updatePassBtn = new Button("Đổi Mật Khẩu");
        updatePassBtn.getStyleClass().addAll("btn-primary");
        updatePassBtn.setMaxWidth(Double.MAX_VALUE);
        updatePassBtn.setOnAction(e -> {
            String oldP = oldPassField.getText();
            String newP = newPassField.getText();
            String confP = confirmPassField.getText();

            if (oldP.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ các trường mật khẩu!");
                return;
            }
            if (!newP.equals(confP)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
                return;
            }
            if (newP.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới phải từ 6 ký tự trở lên!");
                return;
            }

            // Verify old password
            User authUser = userDAO.authenticate(currentUser.getUsername(), oldP);
            if (authUser == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu cũ không chính xác!");
                return;
            }

            if (userDAO.changePassword(currentUser.getId(), newP)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                oldPassField.clear();
                newPassField.clear();
                confirmPassField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi đổi mật khẩu.");
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(10);
        grid.addRow(0, oldPassLabel, oldPassField);
        grid.addRow(1, newPassLabel, newPassField);
        grid.addRow(2, confirmPassLabel, confirmPassField);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        formCard.getChildren().addAll(
                nameLabel, nameRow,
                passTitleLabel, grid, updatePassBtn
        );

        getChildren().addAll(titleLabel, formCard);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
