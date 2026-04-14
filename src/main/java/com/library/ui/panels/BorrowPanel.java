package com.library.ui.panels;

import com.library.dao.*;
import com.library.model.*;
import com.library.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BorrowPanel extends VBox {
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();
    private TableView<BorrowRecord> table;
    private ObservableList<BorrowRecord> recordList;
    private ToggleGroup filterGroup;
    private RadioButton allRb;
    private RadioButton activeRb;
    private RadioButton warningRb;
    private RadioButton overdueRb;
    private TextField searchField;

    public BorrowPanel() {
        setSpacing(16);
        borrowDAO.updateOverdueStatus();
        buildUI();
        loadData("ALL");
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        Label title = new Label("Mượn / Trả sách");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Quản lý việc mượn và trả sách");
        subtitle.getStyleClass().add("page-subtitle");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button borrowBtn = new Button("+ Tạo phiếu mượn");
        borrowBtn.getStyleClass().add("btn-primary");
        borrowBtn.setOnAction(e -> showBorrowDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter
        filterGroup = new ToggleGroup();
        allRb = new RadioButton("Tất cả");
        allRb.setToggleGroup(filterGroup); allRb.setSelected(true); allRb.setUserData("ALL");
        activeRb = new RadioButton("Đang mượn");
        activeRb.setToggleGroup(filterGroup); activeRb.setUserData("ACTIVE");
        warningRb = new RadioButton("Sắp đến hạn");
        warningRb.setToggleGroup(filterGroup); warningRb.setUserData("WARNING");
        overdueRb = new RadioButton("Quá hạn");
        overdueRb.setToggleGroup(filterGroup); overdueRb.setUserData("OVERDUE");

        allRb.setStyle("-fx-text-fill: #cdd6f4;");
        activeRb.setStyle("-fx-text-fill: #a6e3a1;");
        warningRb.setStyle("-fx-text-fill: #f9e2af;");
        overdueRb.setStyle("-fx-text-fill: #f38ba8;");

        filterGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null) loadData(n.getUserData().toString());
        });

        searchField = new TextField();
        searchField.setPromptText("🔍 Tìm theo tên độc giả...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, o, n) -> filterBySearch(n));

        Button exportBtn = new Button("⬇️ Xuất CSV");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> com.library.util.CSVHelper.exportBorrows(recordList));

        toolbar.getChildren().addAll(borrowBtn, exportBtn, searchField, spacer, allRb, activeRb, warningRb, overdueRb);

        // Table
        table = new TableView<>();
        table.setPlaceholder(new Label("Chưa có phiếu mượn nào"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<BorrowRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<BorrowRecord, String> readerCol = new TableColumn<>("Người mượn");
        readerCol.setCellValueFactory(d -> {
            String name = d.getValue().getReaderName();
            if (name == null || name.trim().isEmpty()) name = "[Bỏ trống]";
            return new javafx.beans.property.SimpleStringProperty(name + " (Mã: " + d.getValue().getReaderId() + ")");
        });
        readerCol.setPrefWidth(160);

        TableColumn<BorrowRecord, String> bookCol = new TableColumn<>("Sách");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setPrefWidth(200);

        TableColumn<BorrowRecord, String> borrowCol = new TableColumn<>("Ngày mượn");
        borrowCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            DateUtils.formatDate(d.getValue().getBorrowDate())));
        borrowCol.setPrefWidth(100);

        TableColumn<BorrowRecord, String> dueCol = new TableColumn<>("Hạn trả");
        dueCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            DateUtils.formatDate(d.getValue().getDueDate())));
        dueCol.setPrefWidth(100);

        TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatusDisplay()));
        statusCol.setPrefWidth(90);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("Quá hạn".equals(item)) setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
                else if ("Mất sách".equals(item)) setStyle("-fx-text-fill: #fab387; -fx-font-weight: bold;");
                else if ("Đang mượn".equals(item)) {
                    BorrowRecord r = getTableView().getItems().get(getIndex());
                    long daysToDue = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), r.getDueDate());
                    if (daysToDue >= 0 && daysToDue <= 2) {
                         setStyle("-fx-text-fill: #f9e2af; -fx-font-weight: bold;");
                         setText("Sắp đến hạn (" + daysToDue + " ngày)");
                    } else {
                         setStyle("-fx-text-fill: #a6e3a1;");
                    }
                } else setStyle("-fx-text-fill: #a6adc8;");
            }
        });

        TableColumn<BorrowRecord, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setPrefWidth(220);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button returnBtn = new Button("✅ Trả sách");
            private final Button lostBtn = new Button("⚠ Báo mất");
            private final HBox btnBox = new HBox(6, returnBtn, lostBtn);
            {
                btnBox.setAlignment(Pos.CENTER);
                returnBtn.getStyleClass().add("btn-success");
                returnBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                returnBtn.setOnAction(e -> handleReturn(getTableView().getItems().get(getIndex())));

                lostBtn.setStyle("-fx-background-color: #fab387; -fx-text-fill: #1e1e2e; -fx-padding: 4 10; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                lostBtn.setOnAction(e -> handleLost(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                BorrowRecord rec = getTableView().getItems().get(getIndex());
                if ("RETURNED".equals(rec.getStatus()) || "LOST".equals(rec.getStatus())) {
                    setGraphic(null);
                } else {
                    setGraphic(btnBox);
                }
            }
        });

        table.getColumns().addAll(idCol, readerCol, bookCol, borrowCol, dueCol, statusCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        recordList = FXCollections.observableArrayList();
        table.setItems(recordList);
        getChildren().addAll(new VBox(4, title, subtitle), toolbar, table);
    }

    private void loadData(String filter) {
        List<BorrowRecord> records;
        if ("ACTIVE".equals(filter)) {
            records = borrowDAO.getActiveRecords();
        } else {
            records = borrowDAO.getAll();
            if ("WARNING".equals(filter)) {
                records.removeIf(r -> {
                    if (!"BORROWING".equals(r.getStatus())) return true;
                    long d = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), r.getDueDate());
                    return d < 0 || d > 2;
                });
            } else if ("OVERDUE".equals(filter)) {
                records.removeIf(r -> !"Quá hạn".equals(r.getStatusDisplay()));
            }
        }
        recordList.setAll(records);
        if (searchField != null && !searchField.getText().trim().isEmpty()) {
            filterBySearch(searchField.getText().trim());
        }
    }

    private void filterBySearch(String keyword) {
        String filter = "ALL";
        if (activeRb.isSelected()) filter = "ACTIVE";
        else if (warningRb.isSelected()) filter = "WARNING";
        else if (overdueRb.isSelected()) filter = "OVERDUE";

        List<BorrowRecord> records;
        if ("ACTIVE".equals(filter)) {
            records = borrowDAO.getActiveRecords();
        } else {
            records = borrowDAO.getAll();
            if ("OVERDUE".equals(filter)) {
                records.removeIf(r -> !"Quá hạn".equals(r.getStatusDisplay()));
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            String lowerKw = keyword.toLowerCase();
            records.removeIf(r -> r.getReaderName() == null || !r.getReaderName().toLowerCase().contains(lowerKw));
        }
        recordList.setAll(records);
    }

    private void showBorrowDialog() {
        Dialog<BorrowRecord> dialog = new Dialog<>();
        dialog.setTitle("Tạo phiếu mượn sách");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Tạo phiếu mượn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20)); grid.setPrefWidth(460);

        ComboBox<Reader> readerCombo = new ComboBox<>(FXCollections.observableArrayList(readerDAO.getAll()));
        readerCombo.setMaxWidth(Double.MAX_VALUE);
        readerCombo.setPromptText("Chọn độc giả...");

        List<Book> availableBooks = bookDAO.getAll().stream().filter(b -> b.getAvailable() > 0).toList();
        ComboBox<Book> bookCombo = new ComboBox<>(FXCollections.observableArrayList(availableBooks));
        bookCombo.setMaxWidth(Double.MAX_VALUE);
        bookCombo.setPromptText("Chọn sách...");

        DatePicker borrowDate = new DatePicker(LocalDate.now());
        DatePicker dueDate = new DatePicker(LocalDate.now().plusDays(Constants.MAX_BORROW_DAYS));

        grid.addRow(0, new Label("Độc giả:"), readerCombo);
        grid.addRow(1, new Label("Sách:"), bookCombo);
        grid.addRow(2, new Label("Ngày mượn:"), borrowDate);
        grid.addRow(3, new Label("Hạn trả:"), dueDate);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                if (readerCombo.getValue() == null || bookCombo.getValue() == null) {
                    new Alert(Alert.AlertType.ERROR, "Vui lòng chọn đầy đủ độc giả và sách!").showAndWait();
                    return null;
                }
                BorrowRecord r = new BorrowRecord();
                r.setReaderId(readerCombo.getValue().getId());
                r.setBookId(bookCombo.getValue().getId());
                r.setBorrowDate(borrowDate.getValue());
                r.setDueDate(dueDate.getValue());
                return r;
            }
            return null;
        });

        Optional<BorrowRecord> result = dialog.showAndWait();
        result.ifPresent(r -> {
            if (borrowDAO.borrowBook(r.getReaderId(), r.getBookId(), r.getBorrowDate(), r.getDueDate())) {
                bookDAO.decreaseAvailable(r.getBookId());
                loadData("ALL");
                new Alert(Alert.AlertType.INFORMATION, "Tạo phiếu mượn thành công!").showAndWait();
            }
        });
    }

    private void handleReturn(BorrowRecord record) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Trả sách");
        confirm.setHeaderText("Trả sách: " + record.getBookTitle() + " (Độc giả: " + record.getReaderName() + ")");
        
        long finalFine = 0;
        if ("Quá hạn".equals(record.getStatusDisplay())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), java.time.LocalDate.now());
            if (daysOverdue > 0) {
                finalFine = (long) (daysOverdue * com.library.util.Constants.FINE_PER_DAY);
                confirm.setContentText("⚠️ SÁCH BỊ QUÁ HẠN " + daysOverdue + " NGÀY!\n\n" +
                        "Tổng tiền phạt: " + String.format("%,d", finalFine) + " VNĐ\n\nBạn xác nhận Độc giả này đã Nộp phạt và Trả sách đủ?");
            } else {
                confirm.setContentText("Bạn có chắc chắn phiếu mượn này đã được trả sách đầy đủ?");
            }
        } else {
            confirm.setContentText("Bạn có chắc chắn phiếu mượn này đã được trả sách đầy đủ?");
        }

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (borrowDAO.returnBook(record.getId(), finalFine)) {
                bookDAO.increaseAvailable(record.getBookId());
                loadData("ALL");
                new Alert(Alert.AlertType.INFORMATION, "Trả sách thành công!").showAndWait();
            }
        }
    }

    private void handleLost(BorrowRecord record) {
        // Dialog nhập tiền phạt mất sách
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Báo mất sách");
        dialog.setHeaderText("Sách bị mất: " + record.getBookTitle() + "\nĐộc giả: " + record.getReaderName());

        ButtonType confirmType = new ButtonType("Xác nhận mất sách", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField fineField = new TextField(String.valueOf((long)(Constants.FINE_PER_DAY * 20)));
        fineField.setPromptText("Nhập số tiền phạt (VNĐ)");

        Label noteLabel = new Label("Mặc định: phạt 20 ngày (" + String.format("%,.0f", Constants.FINE_PER_DAY * 20) + " VNĐ).\nBạn có thể thay đổi số tiền.");
        noteLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 11px;");
        noteLabel.setWrapText(true);

        grid.addRow(0, new Label("Tiền phạt (VNĐ):"), fineField);
        grid.add(noteLabel, 0, 1, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == confirmType) {
                try {
                    return Double.parseDouble(fineField.getText().trim());
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Số tiền không hợp lệ!").showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(fine -> {
            Alert confirm = new Alert(Alert.AlertType.WARNING);
            confirm.setTitle("Xác nhận mất sách");
            confirm.setHeaderText("⚠️ Cảnh báo: Hành động này không thể hoàn tác!");
            confirm.setContentText("Sách \"" + record.getBookTitle() + "\" sẽ bị đánh dấu là MẤT SÁCH.\n" +
                    "Số lượng tồn kho sẽ bị giảm vĩnh viễn.\n" +
                    "Tiền phạt: " + String.format("%,.0f", fine) + " VNĐ\n\nBạn có chắc chắn không?");
            confirm.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                if (borrowDAO.markAsLost(record.getId(), fine)) {
                    // Giảm tổng số lượng sách (không tăng lại available vì sách đã mất)
                    bookDAO.decreaseQuantity(record.getBookId());
                    loadData("ALL");
                    new Alert(Alert.AlertType.INFORMATION,
                            "Sách \"" + record.getBookTitle() + "\" đã được đánh dấu MẤT SÁCH.\n" +
                            "Tiền phạt: " + String.format("%,.0f", fine) + " VNĐ").showAndWait();
                }
            }
        });
    }
}
