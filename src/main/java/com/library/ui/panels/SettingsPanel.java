package com.library.ui.panels;

import com.library.util.Constants;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SettingsPanel extends VBox {
    private final BorderPane mainView;

    public SettingsPanel(BorderPane mainView) {
        this.mainView = mainView;
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        Label title = new Label("Cài đặt");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Tùy chỉnh giao diện và cấu hình hệ thống");
        subtitle.getStyleClass().add("page-subtitle");

        // Theme settings
        VBox themeBox = new VBox(12);
        themeBox.getStyleClass().add("card-panel");

        Label themeTitle = new Label("Giao diện");
        themeTitle.getStyleClass().add("section-title");

        HBox themeRow = new HBox(16);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Chế độ giao diện:");
        themeLabel.setStyle("-fx-text-fill: #cdd6f4;");

        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton darkRb = new RadioButton("Dark Mode");
        darkRb.setToggleGroup(themeGroup);
        darkRb.setSelected(true);
        darkRb.setStyle("-fx-text-fill: #cdd6f4;");

        RadioButton lightRb = new RadioButton("Light Mode");
        lightRb.setToggleGroup(themeGroup);
        lightRb.setStyle("-fx-text-fill: #cdd6f4;");

        themeGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == darkRb) applyTheme(Constants.DARK_THEME);
            else applyTheme(Constants.LIGHT_THEME);
        });

        themeRow.getChildren().addAll(themeLabel, darkRb, lightRb);
        themeBox.getChildren().addAll(themeTitle, themeRow);

        // Borrow settings
        VBox borrowBox = new VBox(12);
        borrowBox.getStyleClass().add("card-panel");

        Label borrowTitle = new Label("Cấu hình mượn sách");
        borrowTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        Label maxDaysLabel = new Label("Số ngày mượn tối đa:");
        maxDaysLabel.setStyle("-fx-text-fill: #cdd6f4;");
        Spinner<Integer> maxDaysSpinner = new Spinner<>(1, 90, Constants.MAX_BORROW_DAYS);
        maxDaysSpinner.setEditable(true);
        maxDaysSpinner.setPrefWidth(120);

        Label fineLabel = new Label("Phí phạt/ngày (VNĐ):");
        fineLabel.setStyle("-fx-text-fill: #cdd6f4;");
        Spinner<Double> fineSpinner = new Spinner<>(0, 100000, Constants.FINE_PER_DAY, 1000);
        fineSpinner.setEditable(true);
        fineSpinner.setPrefWidth(120);

        grid.addRow(0, maxDaysLabel, maxDaysSpinner);
        grid.addRow(1, fineLabel, fineSpinner);

        Button saveConfigBtn = new Button("Lưu cấu hình");
        saveConfigBtn.getStyleClass().add("btn-primary");
        saveConfigBtn.setOnAction(e -> {
            Constants.MAX_BORROW_DAYS = maxDaysSpinner.getValue();
            Constants.FINE_PER_DAY = fineSpinner.getValue();
            com.library.util.ConfigManager.saveConfig();
            new Alert(Alert.AlertType.INFORMATION, "Đã lưu cấu hình thành công!", ButtonType.OK).showAndWait();
        });

        borrowBox.getChildren().addAll(borrowTitle, grid, saveConfigBtn);

        // About section
        VBox aboutBox = new VBox(8);
        aboutBox.getStyleClass().add("card-panel");

        Label aboutTitle = new Label("Thông tin ứng dụng");
        aboutTitle.getStyleClass().add("section-title");

        Label appName = new Label(Constants.APP_TITLE);
        appName.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label version = new Label("Phiên bản: " + Constants.APP_VERSION);
        version.setStyle("-fx-text-fill: #a6adc8;");
        Label tech = new Label("Công nghệ: JavaFX 21 + SQLite");
        tech.setStyle("-fx-text-fill: #a6adc8;");
        Label developer = new Label("Ứng dụng quản lý thư viện số");
        developer.setStyle("-fx-text-fill: #a6adc8;");

        aboutBox.getChildren().addAll(aboutTitle, appName, version, tech, developer);

        getChildren().addAll(new VBox(4, title, subtitle), themeBox, borrowBox, aboutBox);
    }

    private void applyTheme(String themePath) {
        if (mainView.getScene() != null) {
            mainView.getScene().getStylesheets().clear();
            String css = getClass().getResource(themePath).toExternalForm();
            mainView.getScene().getStylesheets().add(css);
        }
    }
}
