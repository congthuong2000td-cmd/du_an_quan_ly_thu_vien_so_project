package com.library.ui.panels.reader;

import com.library.dao.BookDAO;
import com.library.dao.BorrowDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.util.DateUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class ReaderBorrowPanel extends VBox {
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final User currentUser;
    private TableView<BorrowRecord> table;
    private ObservableList<BorrowRecord> data;

    public ReaderBorrowPanel(User user) {
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

        Label titleLabel = new Label("⏳ Sách Đang Mượn & Lịch Sử");
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

        TableColumn<BorrowRecord, String> readerCol = new TableColumn<>("Người mượn");
        readerCol.setCellValueFactory(d -> {
            String name = d.getValue().getReaderName();
            return new SimpleStringProperty((name != null && !name.trim().isEmpty()) ? name : currentUser.getFullName());
        });
        readerCol.setMinWidth(140);

        TableColumn<BorrowRecord, String> bookCol = new TableColumn<>("Tên sách");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setMinWidth(250);

        TableColumn<BorrowRecord, String> borrowCol = new TableColumn<>("Ngày mượn");
        borrowCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getBorrowDate() != null ? DateUtils.formatDate(d.getValue().getBorrowDate()) : "—"
        ));
        borrowCol.setMinWidth(120);

        TableColumn<BorrowRecord, String> dueCol = new TableColumn<>("Hạn trả");
        dueCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDueDate() != null ? DateUtils.formatDate(d.getValue().getDueDate()) : "—"
        ));
        dueCol.setMinWidth(120);

        TableColumn<BorrowRecord, String> returnCol = new TableColumn<>("Ngày trả (Thực tế)");
        returnCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getReturnDate() != null ? DateUtils.formatDate(d.getValue().getReturnDate()) : "—"
        ));
        returnCol.setMinWidth(120);

        TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatusDisplay()));
        statusCol.setMinWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Quá hạn".equals(item)) setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
                    else if ("Đang mượn".equals(item)) setStyle("-fx-text-fill: #f9e2af; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<BorrowRecord, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setMinWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button renewBtn = new Button("📅 Gia hạn");
            private final Button returnBtn = new Button("📕 Trả sớm");
            private final HBox btnBox = new HBox(6, renewBtn, returnBtn);
            {
                btnBox.setAlignment(Pos.CENTER);
                renewBtn.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-padding: 4 12; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                returnBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-padding: 4 12; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                renewBtn.setOnAction(e -> {
                    BorrowRecord r = getTableView().getItems().get(getIndex());
                    handleRenew(r);
                });
                returnBtn.setOnAction(e -> {
                    BorrowRecord r = getTableView().getItems().get(getIndex());
                    handleReturnEarly(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BorrowRecord r = getTableView().getItems().get(getIndex());
                    if ("BORROWING".equals(r.getStatus()) || "OVERDUE".equals(r.getStatus())) {
                        setGraphic(btnBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(readerCol, bookCol, borrowCol, dueCol, returnCol, statusCol, actionCol);

        // Empty state
        table.setPlaceholder(new Label("📭 Không tìm thấy phiếu mượn nào hoặc Thủ thư chưa cấp thẻ Độc giả khớp với tên của bạn."));

        getChildren().addAll(header, table);
    }

    private void handleRenew(BorrowRecord r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Gia hạn sách");
        confirm.setHeaderText("Gia hạn: " + r.getBookTitle());
        confirm.setContentText("Bạn có chắc muốn xin gia hạn sách này thêm 7 ngày không?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (borrowDAO.renewBook(r.getId())) {
                    loadData();
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "✅ Gia hạn thành công thêm 7 ngày!", ButtonType.OK);
                    success.setHeaderText(null);
                    success.showAndWait();
                }
            }
        });
    }

    private void handleReturnEarly(BorrowRecord r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Trả sách sớm");
        confirm.setHeaderText("Trả sách: " + r.getBookTitle());
        confirm.setContentText("Bạn có chắc muốn trả cuốn sách này ngay bây giờ không?\nKhông có tiền phạt khi trả sớm.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (borrowDAO.returnBook(r.getId(), 0)) {
                    // Cập nhật số lượng sách có sẵn
                    List<Book> allBooks = bookDAO.getAll();
                    for (Book b : allBooks) {
                        if (b.getId() == r.getBookId()) {
                            b.setAvailable(b.getAvailable() + 1);
                            bookDAO.update(b);
                            break;
                        }
                    }
                    loadData();
                    Alert success = new Alert(Alert.AlertType.INFORMATION,
                            "✅ Trả sách thành công! Cảm ơn bạn đã trả sách đúng hẹn.", ButtonType.OK);
                    success.setHeaderText(null);
                    success.showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR, "❌ Có lỗi xảy ra khi trả sách.", ButtonType.OK).show();
                }
            }
        });
    }

    private void loadData() {
        borrowDAO.updateOverdueStatus(); // Update overdues dynamically when fetching!
        List<BorrowRecord> records = borrowDAO.getByReaderName(currentUser.getFullName());
        data = FXCollections.observableArrayList(records);
        table.setItems(data);
    }
}
