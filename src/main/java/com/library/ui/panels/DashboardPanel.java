package com.library.ui.panels;

import com.library.dao.*;
import com.library.model.BorrowRecord;
import com.library.util.DateUtils;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class DashboardPanel extends VBox {
    private final BookDAO bookDAO = new BookDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();

    public DashboardPanel() {
        setSpacing(20);
        setPadding(new Insets(0));
        borrowDAO.updateOverdueStatus();
        buildUI();
    }

    private void buildUI() {
        // Title
        Label title = new Label("Tổng quan");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Thống kê tổng quan hệ thống thư viện");
        subtitle.getStyleClass().add("page-subtitle");
        VBox titleBox = new VBox(4, title, subtitle);

        // Stats cards
        HBox cardsBox = new HBox(16);
        cardsBox.setAlignment(Pos.CENTER_LEFT);

        int totalBooks = bookDAO.getTotalCount();
        int totalReaders = readerDAO.getTotalCount();
        int activeBorrows = borrowDAO.getActiveBorrowCount();
        int overdueCount = borrowDAO.getOverdueCount();

        cardsBox.getChildren().addAll(
            createCard("\uD83D\uDCDA", String.valueOf(totalBooks), "Tổng số sách", "dash-card-blue"),
            createCard("\uD83D\uDC65", String.valueOf(totalReaders), "Độc giả", "dash-card-green"),
            createCard("\uD83D\uDD04", String.valueOf(activeBorrows), "Đang mượn", "dash-card-peach"),
            createCard("\u26A0\uFE0F", String.valueOf(overdueCount), "Quá hạn", "dash-card-red")
        );

        // Charts row
        HBox chartsRow = new HBox(16);
        chartsRow.setPrefHeight(280);
        HBox.setHgrow(chartsRow, Priority.ALWAYS);

        // Bar chart
        VBox barChartBox = createBorrowChart();
        HBox.setHgrow(barChartBox, Priority.ALWAYS);

        // Pie chart
        VBox pieChartBox = createCategoryPieChart();
        HBox.setHgrow(pieChartBox, Priority.ALWAYS);

        chartsRow.getChildren().addAll(barChartBox, pieChartBox);

        // Recent activities
        VBox recentBox = createRecentActivities();
        VBox.setVgrow(recentBox, Priority.ALWAYS);

        getChildren().addAll(titleBox, cardsBox, chartsRow, recentBox);
    }

    @SuppressWarnings("unchecked")
    private VBox createCard(String icon, String value, String label, String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("dash-card", styleClass);
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("card-icon");
        topRow.getChildren().addAll(valueLabel, spacer, iconLabel);

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("card-label");

        card.getChildren().addAll(topRow, textLabel);
        return card;
    }

    private VBox createBorrowChart() {
        VBox box = new VBox();
        box.getStyleClass().add("card-panel");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Tháng");
        yAxis.setLabel("Lượt mượn");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Lượt mượn sách theo tháng");
        chart.setLegendVisible(false);
        chart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> data = borrowDAO.getBorrowsByMonth();
        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("Chưa có dữ liệu", 0));
        } else {
            data.forEach((month, count) -> series.getData().add(new XYChart.Data<>(month, count)));
        }
        chart.getData().add(series);

        box.getChildren().add(chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return box;
    }

    private VBox createCategoryPieChart() {
        VBox box = new VBox();
        box.getStyleClass().add("card-panel");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Sách theo thể loại");
        pieChart.setAnimated(true);
        pieChart.setLabelsVisible(true);

        Map<String, Integer> data = borrowDAO.getBooksByCategory();
        if (data.isEmpty()) {
            pieChart.getData().add(new PieChart.Data("Chưa có dữ liệu", 1));
        } else {
            data.forEach((name, count) -> pieChart.getData().add(new PieChart.Data(name + " (" + count + ")", count)));
        }

        box.getChildren().add(pieChart);
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        return box;
    }

    @SuppressWarnings("unchecked")
    private VBox createRecentActivities() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card-panel");

        Label sectionTitle = new Label("Hoạt động gần đây");
        sectionTitle.getStyleClass().add("section-title");

        TableView<BorrowRecord> table = new TableView<>();
        table.setPlaceholder(new Label("Chưa có hoạt động nào"));

        TableColumn<BorrowRecord, String> readerCol = new TableColumn<>("Độc giả");
        readerCol.setCellValueFactory(new PropertyValueFactory<>("readerName"));
        readerCol.setPrefWidth(150);

        TableColumn<BorrowRecord, String> bookCol = new TableColumn<>("Sách");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setPrefWidth(200);

        TableColumn<BorrowRecord, String> dateCol = new TableColumn<>("Ngày mượn");
        dateCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(DateUtils.formatDate(data.getValue().getBorrowDate())));
        dateCol.setPrefWidth(110);

        TableColumn<BorrowRecord, String> dueCol = new TableColumn<>("Hạn trả");
        dueCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(DateUtils.formatDate(data.getValue().getDueDate())));
        dueCol.setPrefWidth(110);

        TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatusDisplay()));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(readerCol, bookCol, dateCol, dueCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        List<BorrowRecord> records = borrowDAO.getRecentRecords(10);
        table.setItems(FXCollections.observableArrayList(records));

        VBox.setVgrow(table, Priority.ALWAYS);
        box.getChildren().addAll(sectionTitle, table);
        return box;
    }
}
