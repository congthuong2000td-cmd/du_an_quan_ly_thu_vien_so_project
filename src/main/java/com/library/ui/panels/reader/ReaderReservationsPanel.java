package com.library.ui.panels.reader;

import com.library.dao.ReservationDAO;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.util.DateUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class ReaderReservationsPanel extends VBox {
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final User currentUser;
    private TableView<Reservation> table;
    private ObservableList<Reservation> data;

    public ReaderReservationsPanel(User user) {
        this.currentUser = user;
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

        Label titleLabel = new Label("📋 Sách Đã Đặt");
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

        TableColumn<Reservation, String> bookCol = new TableColumn<>("Tên sách");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setMinWidth(200);

        TableColumn<Reservation, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("bookAuthor"));
        authorCol.setMinWidth(150);

        TableColumn<Reservation, String> dateCol = new TableColumn<>("Ngày đặt");
        dateCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            return new SimpleStringProperty(r.getRequestDate() != null ? DateUtils.formatDate(r.getRequestDate()) : "");
        });
        dateCol.setMinWidth(110);

        TableColumn<Reservation, String> responseDateCol = new TableColumn<>("Ngày phản hồi");
        responseDateCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            return new SimpleStringProperty(r.getResponseDate() != null ? DateUtils.formatDate(r.getResponseDate()) : "—");
        });
        responseDateCol.setMinWidth(110);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusText()));
        statusCol.setMinWidth(130);
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
                    else if (item.contains("Từ chối")) setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #6c7086;");
                }
            }
        });

        TableColumn<Reservation, Void> actionCol = new TableColumn<>("Hành động");
        actionCol.setMinWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("🚫 Hủy");
            {
                cancelBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #f38ba8; -fx-padding: 4 12; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    handleCancel(r);
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
                        setGraphic(cancelBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(bookCol, authorCol, dateCol, responseDateCol, statusCol, actionCol);

        // Empty state info
        table.setPlaceholder(new Label("📭 Bạn chưa đặt sách nào.\nHãy vào Kho sách để đặt sách!"));

        getChildren().addAll(header, table);
    }

    private void loadData() {
        List<Reservation> reservations = reservationDAO.getByUserId(currentUser.getId());
        data = FXCollections.observableArrayList(reservations);
        table.setItems(data);
    }

    private void handleCancel(Reservation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hủy đặt sách");
        confirm.setHeaderText("Hủy đặt: " + r.getBookTitle());
        confirm.setContentText("Bạn có chắc muốn hủy yêu cầu đặt sách này?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (reservationDAO.cancel(r.getId())) {
                    loadData();
                }
            }
        });
    }
}
