package com.library.ui;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.util.Constants;
import com.library.util.ValidationUtils;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginView extends StackPane {
    private final UserDAO userDAO = new UserDAO();
    private Runnable onLoginSuccess;
    private User loggedInUser;

    // Login form fields
    private TextField loginUsername;
    private PasswordField loginPassword;
    private Label loginError;
    private Button loginBtn;

    // Register form fields
    private TextField regUsername;
    private TextField regFullName;
    private PasswordField regPassword;
    private PasswordField regConfirmPassword;
    private Label regError;
    private Label regSuccess;
    private ComboBox<String> regRoleCombo;
    private ComboBox<String> regQuestionCombo;
    private TextField regAnswer;
    private Button regBtn;

    // Forgot password fields
    private VBox forgotCard;
    private TextField forgotUsername;
    private TextField forgotAnswer;
    private PasswordField forgotNewPass;
    private PasswordField forgotConfirmPass;
    private Label forgotError;
    private Label forgotSuccess;
    private Label forgotQuestionLabel;
    private Button forgotBtn;
    private int forgotStep = 1; // 1: Username, 2: Answer, 3: New Password
    private User recoveryUser;

    private static final String[] SECURITY_QUESTIONS = {
        "Tên trường học đầu tiên của bạn?",
        "Tên thú cưng đầu tiên của bạn?",
        "Món ăn yêu thích nhất của bạn?",
        "Nơi sinh của mẹ bạn?",
        "Tên người bạn thân nhất thời thơ ấu?"
    };

    // Cards
    private VBox loginCard;
    private VBox registerCard;

    public LoginView() {
        getStyleClass().add("login-bg");
        buildUI();
    }

    private void buildUI() {
        // ========== LOGIN CARD ==========
        loginCard = new VBox(18);
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(400);
        loginCard.setMaxHeight(Region.USE_PREF_SIZE);

        ImageView logoView = null;
        try {
            Image logoImg = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logoView = new ImageView(logoImg);
            logoView.setFitWidth(80);
            logoView.setFitHeight(80);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {}
        
        javafx.scene.Node icon = logoView != null ? logoView : new Label("\uD83D\uDCDA");
        if (icon instanceof Label) icon.getStyleClass().add("login-icon");
        Label title = new Label(Constants.APP_TITLE);
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Đăng nhập để tiếp tục");
        subtitle.getStyleClass().add("login-subtitle");
        VBox titleBox = new VBox(5, icon, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Tên đăng nhập");
        userLabel.getStyleClass().add("form-label");
        loginUsername = new TextField();
        loginUsername.setPromptText("Nhập tên đăng nhập...");
        loginUsername.setPrefHeight(42);

        Label passLabel = new Label("Mật khẩu");
        passLabel.getStyleClass().add("form-label");
        loginPassword = new PasswordField();
        loginPassword.setPromptText("Nhập mật khẩu...");
        loginPassword.setPrefHeight(42);

        loginError = new Label();
        loginError.getStyleClass().add("login-error");
        loginError.setVisible(false);
        loginError.setManaged(false);

        loginBtn = new Button("Đăng nhập");
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setPrefHeight(44);
        loginBtn.setOnAction(e -> handleLogin());

        // Register link
        HBox registerLinkBox = new HBox(5);
        registerLinkBox.setAlignment(Pos.CENTER);
        Label noAccountLabel = new Label("Chưa có tài khoản?");
        noAccountLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 13px;");
        Hyperlink registerLink = new Hyperlink("Đăng ký ngay");
        registerLink.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 13px; -fx-border-color: transparent;");
        registerLink.setOnAction(e -> showRegisterForm());
        
        Hyperlink forgotLink = new Hyperlink("Quên mật khẩu?");
        forgotLink.setStyle("-fx-text-fill: #94e2d5; -fx-font-size: 13px; -fx-border-color: transparent;");
        forgotLink.setOnAction(e -> showForgotForm());
        
        registerLinkBox.getChildren().addAll(noAccountLabel, registerLink, new Label("|"), forgotLink);



        VBox formBox = new VBox(5, userLabel, loginUsername,
                new Region() {{ setPrefHeight(3); }},
                passLabel, loginPassword);

        loginCard.getChildren().addAll(titleBox, new Separator(), formBox,
                loginError, loginBtn, registerLinkBox);

        // ========== REGISTER CARD ==========
        registerCard = new VBox(14);
        registerCard.getStyleClass().add("login-card");
        registerCard.setAlignment(Pos.CENTER);
        registerCard.setMaxWidth(420);
        registerCard.setMaxHeight(Region.USE_PREF_SIZE);
        registerCard.setVisible(false);
        registerCard.setManaged(false);

        Label regIcon = new Label("\uD83D\uDC64");
        regIcon.getStyleClass().add("login-icon");
        Label regTitle = new Label("Tạo tài khoản mới");
        regTitle.getStyleClass().add("login-title");
        regTitle.setStyle("-fx-font-size: 24px;");
        Label regSubtitle = new Label("Điền thông tin để đăng ký tài khoản");
        regSubtitle.getStyleClass().add("login-subtitle");
        VBox regTitleBox = new VBox(5, regIcon, regTitle, regSubtitle);
        regTitleBox.setAlignment(Pos.CENTER);

        // Full name
        Label nameLabel = new Label("Họ và tên");
        nameLabel.getStyleClass().add("form-label");
        regFullName = new TextField();
        regFullName.setPromptText("Nhập họ và tên...");
        regFullName.setPrefHeight(42);

        // Username
        Label regUserLabel = new Label("Tên đăng nhập");
        regUserLabel.getStyleClass().add("form-label");
        regUsername = new TextField();
        regUsername.setPromptText("Tạo tên đăng nhập...");
        regUsername.setPrefHeight(42);

        // Password
        Label regPassLabel = new Label("Mật khẩu");
        regPassLabel.getStyleClass().add("form-label");
        regPassword = new PasswordField();
        regPassword.setPromptText("Tạo mật khẩu (ít nhất 6 ký tự)...");
        regPassword.setPrefHeight(42);

        // Confirm password
        Label regConfirmLabel = new Label("Xác nhận mật khẩu");
        regConfirmLabel.getStyleClass().add("form-label");
        regConfirmPassword = new PasswordField();
        regConfirmPassword.setPromptText("Nhập lại mật khẩu...");
        regConfirmPassword.setPrefHeight(42);

        // Role selection
        Label regRoleLabel = new Label("Vai trò");
        regRoleLabel.getStyleClass().add("form-label");
        regRoleCombo = new ComboBox<>();
        regRoleCombo.getItems().addAll("Độc giả (Reader)", "Thủ thư (Librarian)");
        regRoleCombo.getSelectionModel().selectFirst();
        regRoleCombo.setPrefHeight(42);
        regRoleCombo.setMaxWidth(Double.MAX_VALUE);

        // Security Question
        Label regQuestLabel = new Label("Câu hỏi bảo mật");
        regQuestLabel.getStyleClass().add("form-label");
        regQuestionCombo = new ComboBox<>();
        regQuestionCombo.getItems().addAll(SECURITY_QUESTIONS);
        regQuestionCombo.getSelectionModel().selectFirst();
        regQuestionCombo.setPrefHeight(42);
        regQuestionCombo.setMaxWidth(Double.MAX_VALUE);

        Label regAnsLabel = new Label("Câu trả lời");
        regAnsLabel.getStyleClass().add("form-label");
        regAnswer = new TextField();
        regAnswer.setPromptText("Nhập câu trả lời bảo mật...");
        regAnswer.setPrefHeight(42);

        // Error & Success labels
        regError = new Label();
        regError.getStyleClass().add("login-error");
        regError.setVisible(false);
        regError.setManaged(false);

        regSuccess = new Label();
        regSuccess.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 12px;");
        regSuccess.setVisible(false);
        regSuccess.setManaged(false);

        // Register button
        regBtn = new Button("Đăng ký");
        regBtn.getStyleClass().add("login-btn");
        regBtn.setStyle("-fx-background-color: linear-gradient(to right, #a6e3a1, #94e2d5);");
        regBtn.setPrefHeight(44);
        regBtn.setOnAction(e -> handleRegister());

        // Back to login link
        HBox loginLinkBox = new HBox(5);
        loginLinkBox.setAlignment(Pos.CENTER);
        Label hasAccountLabel = new Label("Đã có tài khoản?");
        hasAccountLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 13px;");
        Hyperlink loginLink = new Hyperlink("Đăng nhập");
        loginLink.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 13px; -fx-border-color: transparent;");
        loginLink.setOnAction(e -> showLoginForm());
        loginLinkBox.getChildren().addAll(hasAccountLabel, loginLink);

        VBox regFormBox = new VBox(4,
                nameLabel, regFullName,
                regUserLabel, regUsername,
                regPassLabel, regPassword,
                regConfirmLabel, regConfirmPassword,
                regRoleLabel, regRoleCombo,
                regQuestLabel, regQuestionCombo,
                regAnsLabel, regAnswer);

        registerCard.getChildren().addAll(regTitleBox, new Separator(), regFormBox,
                regError, regSuccess, regBtn, loginLinkBox);

        // ========== FORGOT PASSWORD CARD ==========
        forgotCard = new VBox(18);
        forgotCard.getStyleClass().add("login-card");
        forgotCard.setAlignment(Pos.CENTER);
        forgotCard.setMaxWidth(400);
        forgotCard.setMaxHeight(Region.USE_PREF_SIZE);
        forgotCard.setVisible(false);
        forgotCard.setManaged(false);

        Label forgotIcon = new Label("\uD83D\uDD11");
        forgotIcon.getStyleClass().add("login-icon");
        Label forgotTitle = new Label("Khôi phục mật khẩu");
        forgotTitle.getStyleClass().add("login-title");
        Label forgotSubtitle = new Label("Lấy lại mật khẩu qua câu hỏi bảo mật");
        forgotSubtitle.getStyleClass().add("login-subtitle");
        VBox forgotTitleBox = new VBox(5, forgotIcon, forgotTitle, forgotSubtitle);
        forgotTitleBox.setAlignment(Pos.CENTER);

        forgotError = new Label();
        forgotError.getStyleClass().add("login-error");
        forgotError.setVisible(false);
        forgotError.setManaged(false);

        forgotSuccess = new Label();
        forgotSuccess.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 13px; -fx-text-alignment: center;");
        forgotSuccess.setVisible(false);
        forgotSuccess.setManaged(false);

        forgotUsername = new TextField();
        forgotUsername.setPromptText("Nhập tên đăng nhập của bạn...");
        forgotUsername.setPrefHeight(42);

        forgotQuestionLabel = new Label();
        forgotQuestionLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 14px; -fx-font-weight: bold;");
        forgotQuestionLabel.setWrapText(true);
        forgotAnswer = new TextField();
        forgotAnswer.setPromptText("Nhập câu trả lời...");
        forgotAnswer.setPrefHeight(42);

        forgotNewPass = new PasswordField();
        forgotNewPass.setPromptText("Mật khẩu mới...");
        forgotNewPass.setPrefHeight(42);
        forgotConfirmPass = new PasswordField();
        forgotConfirmPass.setPromptText("Xác nhận mật khẩu mới...");
        forgotConfirmPass.setPrefHeight(42);

        forgotBtn = new Button("Tiếp tục");
        forgotBtn.getStyleClass().add("login-btn");
        forgotBtn.setPrefHeight(44);
        forgotBtn.setOnAction(e -> handleForgot());

        Hyperlink forgotBackLink = new Hyperlink("Quay lại đăng nhập");
        forgotBackLink.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 13px; -fx-border-color: transparent;");
        forgotBackLink.setOnAction(e -> showLoginForm());

        forgotCard.getChildren().addAll(forgotTitleBox, new Separator(),
                forgotError, forgotSuccess, forgotUsername, forgotBtn, forgotBackLink);

        // Layout
        setAlignment(Pos.CENTER);
        getChildren().addAll(loginCard, registerCard, forgotCard);

        // Key bindings
        loginPassword.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        loginUsername.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) loginPassword.requestFocus(); });
        regConfirmPassword.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });
    }

    // ========== ANIMATIONS ==========

    private void showRegisterForm() {
        // Fade out login, fade in register
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loginCard);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            loginCard.setVisible(false);
            loginCard.setManaged(false);
            registerCard.setVisible(true);
            registerCard.setManaged(true);
            registerCard.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), registerCard);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();
            regFullName.requestFocus();
        });
        fadeOut.play();
        clearRegisterForm();
    }

    private void showLoginForm() {
        VBox activeCard = registerCard.isVisible() ? registerCard : forgotCard;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), activeCard);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            activeCard.setVisible(false);
            activeCard.setManaged(false);
            loginCard.setVisible(true);
            loginCard.setManaged(true);
            loginCard.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), loginCard);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();
            loginUsername.requestFocus();
        });
        fadeOut.play();
    }

    private void showForgotForm() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loginCard);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            loginCard.setVisible(false);
            loginCard.setManaged(false);
            forgotCard.setVisible(true);
            forgotCard.setManaged(true);
            forgotCard.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), forgotCard);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();
            resetForgotCard();
        });
        fadeOut.play();
    }

    private void resetForgotCard() {
        forgotStep = 1;
        recoveryUser = null;
        forgotError.setVisible(false);
        forgotError.setManaged(false);
        forgotSuccess.setVisible(false);
        forgotSuccess.setManaged(false);
        forgotUsername.clear();
        forgotUsername.setVisible(true);
        forgotUsername.setManaged(true);
        forgotUsername.requestFocus();
        
        // Remove old dynamic fields if any
        forgotCard.getChildren().removeAll(forgotQuestionLabel, forgotAnswer, forgotNewPass, forgotConfirmPass);
        // Ensure button is at right place
        int btnIdx = forgotCard.getChildren().indexOf(forgotBtn);
        if (btnIdx != -1) {
            forgotCard.getChildren().set(btnIdx, forgotBtn);
        }
        forgotBtn.setText("Tiếp tục");
        forgotBtn.setVisible(true);
    }

    // ========== HANDLERS ==========

    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Đang đăng nhập...");

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            if (!user.isActive()) {
                showLoginError("⏳ Tài khoản đang chờ Admin phê duyệt!\nVui lòng liên hệ quản trị viên.");
                loginBtn.setDisable(false);
                loginBtn.setText("Đăng nhập");
                return;
            }
            loggedInUser = user;
            if (onLoginSuccess != null) onLoginSuccess.run();
        } else {
            showLoginError("Sai tên đăng nhập hoặc mật khẩu!");
            loginBtn.setDisable(false);
            loginBtn.setText("Đăng nhập");
        }
    }

    private void handleRegister() {
        String fullName = regFullName.getText().trim();
        String username = regUsername.getText().trim();
        String password = regPassword.getText().trim();
        String confirmPassword = regConfirmPassword.getText().trim();

        // Validation
        if (ValidationUtils.isNullOrEmpty(fullName)) {
            showRegError("Vui lòng nhập họ và tên!");
            return;
        }
        if (ValidationUtils.isNullOrEmpty(username)) {
            showRegError("Vui lòng nhập tên đăng nhập!");
            return;
        }
        if (username.length() < 3) {
            showRegError("Tên đăng nhập phải có ít nhất 3 ký tự!");
            return;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showRegError("Tên đăng nhập chỉ được chứa chữ cái, số và dấu _");
            return;
        }
        if (password.length() < 6) {
            showRegError("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showRegError("Mật khẩu xác nhận không khớp!");
            return;
        }
        if (userDAO.usernameExists(username)) {
            showRegError("Tên đăng nhập \"" + username + "\" đã tồn tại!");
            return;
        }

        regBtn.setDisable(true);
        regBtn.setText("Đang tạo tài khoản...");

        String roleStr = regRoleCombo.getValue();
        String selectedRole = roleStr.contains("Reader") ? Constants.ROLE_READER : Constants.ROLE_LIBRARIAN;
        String question = regQuestionCombo.getValue();
        String answer = regAnswer.getText().trim();

        if (answer.isEmpty()) {
            showRegError("Vui lòng nhập câu trả lời bảo mật!");
            return;
        }

        User newUser = new User(username, password, fullName, selectedRole, question, answer);
        if (userDAO.insert(newUser)) {
            hideRegError();
            regSuccess.setText("✅ Đăng ký thành công!\nTài khoản cần được Admin phê duyệt trước khi sử dụng.");
            regSuccess.setVisible(true);
            regSuccess.setManaged(true);

            // Auto redirect to login after 1.5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                showLoginForm();
                loginUsername.setText(username);
                loginPassword.requestFocus();
            });
            pause.play();
        } else {
            showRegError("Đăng ký thất bại! Vui lòng thử lại.");
        }

        regBtn.setDisable(false);
        regBtn.setText("Đăng ký");
    }

    private void handleForgot() {
        forgotError.setVisible(false);
        forgotError.setManaged(false);

        if (forgotStep == 1) {
            String username = forgotUsername.getText().trim();
            if (username.isEmpty()) {
                showForgotError("Vui lòng nhập tên đăng nhập!");
                return;
            }
            recoveryUser = userDAO.getUserByUsername(username);
            if (recoveryUser == null) {
                showForgotError("Tên đăng nhập không tồn tại!");
                return;
            }
            
            // Transition to Step 2
            forgotStep = 2;
            forgotUsername.setVisible(false);
            forgotUsername.setManaged(false);
            
            forgotQuestionLabel.setText("Câu hỏi: " + recoveryUser.getSecurityQuestion());
            forgotAnswer.clear();
            
            int btnIdx = forgotCard.getChildren().indexOf(forgotBtn);
            forgotCard.getChildren().add(btnIdx, forgotQuestionLabel);
            forgotCard.getChildren().add(btnIdx + 1, forgotAnswer);
            forgotBtn.setText("Xác thực");
            forgotAnswer.requestFocus();

        } else if (forgotStep == 2) {
            String answer = forgotAnswer.getText().trim();
            if (answer.equalsIgnoreCase(recoveryUser.getSecurityAnswer())) {
                // Transition to Step 3
                forgotStep = 3;
                forgotQuestionLabel.setVisible(false);
                forgotQuestionLabel.setManaged(false);
                forgotAnswer.setVisible(false);
                forgotAnswer.setManaged(false);
                
                int btnIdx = forgotCard.getChildren().indexOf(forgotBtn);
                forgotCard.getChildren().add(btnIdx, forgotNewPass);
                forgotCard.getChildren().add(btnIdx + 1, forgotConfirmPass);
                forgotBtn.setText("Đổi mật khẩu");
                forgotNewPass.requestFocus();
            } else {
                showForgotError("Câu trả lời không chính xác!");
            }
        } else if (forgotStep == 3) {
            String pass = forgotNewPass.getText().trim();
            String confirm = forgotConfirmPass.getText().trim();
            
            if (pass.length() < 6) {
                showForgotError("Mật khẩu phải có ít nhất 6 ký tự!");
                return;
            }
            if (!pass.equals(confirm)) {
                showForgotError("Mật khẩu xác nhận không khớp!");
                return;
            }
            
            if (userDAO.resetPasswordByUsername(recoveryUser.getUsername(), pass)) {
                forgotNewPass.setVisible(false);
                forgotNewPass.setManaged(false);
                forgotConfirmPass.setVisible(false);
                forgotConfirmPass.setManaged(false);
                forgotBtn.setVisible(false);
                forgotBtn.setManaged(false);
                
                forgotSuccess.setText("✅ Đổi mật khẩu thành công!\nBạn có thể đăng nhập ngay.");
                forgotSuccess.setVisible(true);
                forgotSuccess.setManaged(true);
                
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> showLoginForm());
                pause.play();
            } else {
                showForgotError("Có lỗi xảy ra, vui lòng thử lại!");
            }
        }
    }

    private void showForgotError(String msg) {
        forgotError.setText(msg);
        forgotError.setVisible(true);
        forgotError.setManaged(true);
        // Shake
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), forgotCard);
        shake.setByX(10); shake.setCycleCount(6); shake.setAutoReverse(true);
        shake.setOnFinished(e -> forgotCard.setTranslateX(0));
        shake.play();
    }

    // ========== HELPERS ==========

    private void showLoginError(String msg) {
        loginError.setText(msg);
        loginError.setVisible(true);
        loginError.setManaged(true);
        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginCard);
        shake.setByX(10); shake.setCycleCount(6); shake.setAutoReverse(true);
        shake.setOnFinished(e -> loginCard.setTranslateX(0));
        shake.play();
    }

    private void showRegError(String msg) {
        regError.setText(msg);
        regError.setVisible(true);
        regError.setManaged(true);
        regSuccess.setVisible(false);
        regSuccess.setManaged(false);
        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), registerCard);
        shake.setByX(10); shake.setCycleCount(6); shake.setAutoReverse(true);
        shake.setOnFinished(e -> registerCard.setTranslateX(0));
        shake.play();
    }

    private void hideRegError() {
        regError.setVisible(false);
        regError.setManaged(false);
    }

    private void clearRegisterForm() {
        regFullName.clear();
        regUsername.clear();
        regPassword.clear();
        regConfirmPassword.clear();
        regError.setVisible(false);
        regError.setManaged(false);
        regSuccess.setVisible(false);
        regSuccess.setManaged(false);
        regBtn.setDisable(false);
        regBtn.setText("Đăng ký");
    }

    public void setOnLoginSuccess(Runnable action) { this.onLoginSuccess = action; }
    public User getLoggedInUser() { return loggedInUser; }

    public void reset() {
        loginUsername.clear();
        loginPassword.clear();
        loginError.setVisible(false);
        loginError.setManaged(false);
        loginBtn.setDisable(false);
        loginBtn.setText("Đăng nhập");
        // Reset to login form
        registerCard.setVisible(false);
        registerCard.setManaged(false);
        loginCard.setVisible(true);
        loginCard.setManaged(true);
        loginCard.setOpacity(1);
        clearRegisterForm();
    }
}
