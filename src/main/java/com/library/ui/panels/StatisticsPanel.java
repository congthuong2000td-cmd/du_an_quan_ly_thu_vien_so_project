package com.library.ui.panels;

import com.library.dao.BorrowDAO;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Map;

public class StatisticsPanel extends VBox {
    private final BorrowDAO borrowDAO = new BorrowDAO();

    public StatisticsPanel() {
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        Label title = new Label("Thống kê");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Biểu đồ và thống kê hoạt động thư viện");
        subtitle.getStyleClass().add("page-subtitle");

        // Charts
        VBox chartsContainer = new VBox(20);

        // Bar chart - borrows by month
        VBox barBox = new VBox();
        barBox.getStyleClass().add("card-panel");
        barBox.setPrefHeight(300);
        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis yAxis1 = new NumberAxis();
        xAxis1.setLabel("Tháng");
        yAxis1.setLabel("Lượt mượn");
        BarChart<String, Number> barChart = new BarChart<>(xAxis1, yAxis1);
        barChart.setTitle("Lượt mượn sách theo tháng");
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        Map<String, Integer> monthData = borrowDAO.getBorrowsByMonth();
        if (monthData.isEmpty()) {
            series1.getData().add(new XYChart.Data<>("Chưa có dữ liệu", 0));
        } else {
            monthData.forEach((m, c) -> series1.getData().add(new XYChart.Data<>(m, c)));
        }
        barChart.getData().add(series1);
        VBox.setVgrow(barChart, Priority.ALWAYS);
        barBox.getChildren().add(barChart);

        // HBox for pie chart + horizontal bar
        HBox chartsRow = new HBox(20);
        chartsRow.setPrefHeight(300);

        // Pie chart - books by category
        VBox pieBox = new VBox();
        pieBox.getStyleClass().add("card-panel");
        HBox.setHgrow(pieBox, Priority.ALWAYS);
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Sách theo thể loại");
        pieChart.setAnimated(true);
        Map<String, Integer> catData = borrowDAO.getBooksByCategory();
        if (catData.isEmpty()) {
            pieChart.getData().add(new PieChart.Data("Chưa có", 1));
        } else {
            catData.forEach((n, c) -> pieChart.getData().add(new PieChart.Data(n + " (" + c + ")", c)));
        }
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        pieBox.getChildren().add(pieChart);

        // Top borrowed books
        VBox topBox = new VBox(10);
        topBox.getStyleClass().add("card-panel");
        HBox.setHgrow(topBox, Priority.ALWAYS);
        Label topTitle = new Label("Top sách được mượn nhiều nhất");
        topTitle.getStyleClass().add("section-title");
        topBox.getChildren().add(topTitle);

        Map<String, Integer> topBooks = borrowDAO.getTopBorrowedBooks(10);
        if (topBooks.isEmpty()) {
            Label noData = new Label("Chưa có dữ liệu");
            noData.setStyle("-fx-text-fill: #6c7086;");
            topBox.getChildren().add(noData);
        } else {
            int rank = 1;
            int maxCount = topBooks.values().stream().mapToInt(i -> i).max().orElse(1);
            for (Map.Entry<String, Integer> entry : topBooks.entrySet()) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);

                Label rankLabel = new Label("#" + rank);
                rankLabel.setMinWidth(30);
                rankLabel.setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold;");

                Label nameLabel = new Label(entry.getKey());
                nameLabel.setStyle("-fx-text-fill: #cdd6f4;");
                nameLabel.setMaxWidth(200);

                // Progress bar
                ProgressBar pb = new ProgressBar((double) entry.getValue() / maxCount);
                pb.setPrefWidth(120);
                pb.setPrefHeight(8);
                pb.setStyle("-fx-accent: #89b4fa;");
                HBox.setHgrow(pb, Priority.ALWAYS);

                Label countLabel = new Label(entry.getValue() + " lượt");
                countLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

                item.getChildren().addAll(rankLabel, nameLabel, pb, countLabel);
                topBox.getChildren().add(item);
                rank++;
            }
        }

        chartsRow.getChildren().addAll(pieBox, topBox);

        chartsContainer.getChildren().addAll(barBox, chartsRow);

        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(new VBox(4, title, subtitle), scrollPane);
    }
}
