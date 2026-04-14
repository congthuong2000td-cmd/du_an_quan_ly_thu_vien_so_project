package com.library;

import com.library.dao.DatabaseManager;
import com.library.model.User;
import com.library.ui.LoginView;
import com.library.ui.MainView;
import com.library.ui.ReaderMainView;
import com.library.util.Constants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Load config and initialize database
        com.library.util.ConfigManager.loadConfig();
        DatabaseManager.getInstance().initializeDatabase();

        // Show login
        showLogin();

        stage.setTitle(Constants.APP_TITLE);
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch(Exception e) {}
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
    }

    private void showLogin() {
        LoginView loginView = new LoginView();
        loginView.setOnLoginSuccess(() -> {
            User user = loginView.getLoggedInUser();

            if (Constants.ROLE_READER.equals(user.getRole())) {
                // Reader gets a different UI
                ReaderMainView readerView = new ReaderMainView(user);
                readerView.setOnLogout(this::showLogin);
                mainScene = new Scene(readerView, 1280, 800);
            } else {
                // Admin / Librarian
                MainView mainView = new MainView(user);
                mainView.setOnLogout(this::showLogin);
                mainScene = new Scene(mainView, 1280, 800);
            }

            applyTheme(mainScene, Constants.DARK_THEME);
            primaryStage.setScene(mainScene);
        });

        loginScene = new Scene(loginView, 1280, 800);
        applyTheme(loginScene, Constants.DARK_THEME);
        primaryStage.setScene(loginScene);
    }

    private void applyTheme(Scene scene, String themePath) {
        String css = getClass().getResource(themePath).toExternalForm();
        scene.getStylesheets().add(css);
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
