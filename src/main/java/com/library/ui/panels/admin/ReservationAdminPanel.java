package com.library.ui.panels.admin;

import com.library.dao.ReservationDAO;
import com.library.model.Reservation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class ReservationAdminPanel extends VBox {
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private TableView<Reservation> table;
    private ObservableList<Reservation> data;

    public ReservationAdminPanel() {
        setSpacing(0);
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

        Label titleLabel = new Label("📋 Quản Lý Đặt Sách");
        titleLabel.getStyleClass().add("panel-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Làm mới");
        refreshBtn.getStyleClass().add("action-btn");
        refreshBtn.setOnAction(e -> loadData());

        header.getChildren().addAll(titleLabel, spacer, refreshBtn);

        // Table
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Reservation, String> userCol = new TableColumn<>("Người đọc");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userCol.setMinWidth(150);

        TableColumn<Reservation, String> bookCol = new TableColumn<>("Sách");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setMinWidth(200);

        TableColumn<Reservation, String> dateCol = new TableColumn<>("Ngày đặt");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequestDate() != null ? d.getValue().getRequestDate().toString() : ""));
        dateCol.setMinWidth(110);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatusText()));
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
                    if (item.contains("Chờ")) setStyle("-fx-text-fill: #fab387; -fx-font-weight: bold;");
                    else if (item.contains("duyệt")) setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");
                    else if (item.contains("Từ chối") || item.contains("hủy")) setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #6c7086;");
                }
            }
        });

        TableColumn<Reservation, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setMinWidth(160);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("✅ Duyệt");
            private final Button rejectBtn = new Button("❌ Từ chối");
            private final HBox box = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e; -fx-padding: 4 8; -fx-background-radius: 6; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e; -fx-padding: 4 8; -fx-background-radius: 6; -fx-cursor: hand;");
                approveBtn.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    if (reservationDAO.approve(r.getId())) loadData();
                });
                rejectBtn.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    if (reservationDAO.reject(r.getId())) loadData();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation r = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(r.getStatus())) {
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(userCol, bookCol, dateCol, statusCol, actionCol);

        getChildren().addAll(header, table);
    }

    private void loadData() {
        List<Reservation> reservations = reservationDAO.getAll();
        data = FXCollections.observableArrayList(reservations);
        table.setItems(data);
    }
}
