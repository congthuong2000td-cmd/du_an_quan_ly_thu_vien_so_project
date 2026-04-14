package com.library.ui.panels;

import com.library.dao.BookDAO;
import com.library.dao.CategoryDAO;
import com.library.model.Book;
import com.library.model.Category;
import com.library.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Optional;
import com.library.util.CSVHelper;

public class BookPanel extends VBox {
    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private TableView<Book> table;
    private ObservableList<Book> bookList;
    private TextField searchField;

    public BookPanel() {
        setSpacing(16);
        buildUI();
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        // Title
        Label title = new Label("Quản lý Sách");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Thêm, sửa, xóa và tìm kiếm sách trong thư viện");
        subtitle.getStyleClass().add("page-subtitle");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.getStyleClass().add("search-box");
        searchField.setPromptText("\uD83D\uDD0D Tìm kiếm sách...");
        searchField.textProperty().addListener((obs, o, n) -> handleSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button addBtn = new Button("+ Thêm sách");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showBookDialog(null));

        Button refreshBtn = new Button("\uD83D\uDD04 Làm mới");
        refreshBtn.getStyleClass().add("btn-success");
        refreshBtn.setOnAction(e -> loadData());

        Button importBtn = new Button("📥 Nhập CSV");
        importBtn.getStyleClass().add("btn-primary");
        importBtn.setOnAction(e -> handleImportCSV());

        Button exportBtn = new Button("📤 Xuất CSV");
        exportBtn.getStyleClass().add("btn-primary");
        exportBtn.setOnAction(e -> handleExportCSV());

        toolbar.getChildren().addAll(searchField, addBtn, importBtn, exportBtn, refreshBtn);

        // Table
        table = new TableView<>();
        table.setPlaceholder(new Label("Chưa có sách nào trong thư viện"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Book, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(140);

        TableColumn<Book, String> titleCol = new TableColumn<>("Tên sách");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Book, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(150);

        TableColumn<Book, String> catCol = new TableColumn<>("Thể loại");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        catCol.setPrefWidth(100);

        TableColumn<Book, Integer> qtyCol = new TableColumn<>("Số lượng");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<Book, Integer> availCol = new TableColumn<>("Còn lại");
        availCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availCol.setPrefWidth(80);

        TableColumn<Book, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setPrefWidth(160);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button delBtn = new Button("Xóa");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().add("btn-primary");
                editBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
                box.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showBookDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, isbnCol, titleCol, authorCol, catCol, qtyCol, availCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Info bar
        Label infoLabel = new Label();
        infoLabel.getStyleClass().add("page-subtitle");
        bookList = FXCollections.observableArrayList();
        bookList.addListener((javafx.collections.ListChangeListener<Book>) c ->
            infoLabel.setText("Tổng: " + bookList.size() + " sách"));

        getChildren().addAll(new VBox(4, title, subtitle), toolbar, table, infoLabel);
    }

    private void loadData() {
        bookList.setAll(bookDAO.getAll());
        table.setItems(bookList);
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
        } else {
            bookList.setAll(bookDAO.search(keyword));
        }
    }

    private void handleDelete(Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sách: " + book.getTitle());
        confirm.setContentText("Bạn có chắc chắn muốn xóa sách này?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (bookDAO.delete(book.getId())) {
                loadData();
                showInfo("Đã xóa sách thành công!");
            } else {
                showError("Không thể xóa sách. Có thể sách đang được mượn.");
            }
        }
    }

    private void showBookDialog(Book book) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(book == null ? "Thêm sách mới" : "Sửa thông tin sách");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);

        TextField isbnField = new TextField(book != null ? book.getIsbn() : "");
        isbnField.setPromptText("VD: 978-604-1-00001");
        TextField titleField = new TextField(book != null ? book.getTitle() : "");
        titleField.setPromptText("Nhập tên sách");
        TextField authorField = new TextField(book != null ? book.getAuthor() : "");
        authorField.setPromptText("Nhập tên tác giả");
        TextField publisherField = new TextField(book != null ? book.getPublisher() : "");
        publisherField.setPromptText("Nhập NXB");
        TextField yearField = new TextField(book != null ? String.valueOf(book.getYear()) : "");
        yearField.setPromptText("VD: 2024");
        Spinner<Integer> qtySpinner = new Spinner<>(1, 1000, book != null ? book.getQuantity() : 1);
        qtySpinner.setEditable(true);

        ComboBox<Category> catCombo = new ComboBox<>(FXCollections.observableArrayList(categoryDAO.getAll()));
        catCombo.setMaxWidth(Double.MAX_VALUE);
        if (book != null) {
            catCombo.getItems().stream()
                .filter(c -> c.getId() == book.getCategoryId())
                .findFirst().ifPresent(catCombo::setValue);
        }

        TextArea descArea = new TextArea(book != null ? book.getDescription() : "");
        descArea.setPromptText("Mô tả sách...");
        descArea.setPrefRowCount(3);

        TextArea contentArea = new TextArea(book != null ? book.getContent() : "");
        contentArea.setPromptText("Nội dung sách để đọc online (bỏ trống nếu không có)...");
        contentArea.setPrefRowCount(5);

        int row = 0;
        grid.addRow(row++, new Label("ISBN:"), isbnField);
        grid.addRow(row++, new Label("Tên sách:"), titleField);
        grid.addRow(row++, new Label("Tác giả:"), authorField);
        grid.addRow(row++, new Label("NXB:"), publisherField);
        grid.addRow(row++, new Label("Năm XB:"), yearField);
        grid.addRow(row++, new Label("Thể loại:"), catCombo);
        grid.addRow(row++, new Label("Số lượng:"), qtySpinner);
        grid.addRow(row++, new Label("Mô tả:"), descArea);
        grid.addRow(row++, new Label("Nội dung\nOnline:"), contentArea);

        GridPane.setHgrow(isbnField, Priority.ALWAYS);
        GridPane.setHgrow(titleField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                if (ValidationUtils.isNullOrEmpty(titleField.getText()) || ValidationUtils.isNullOrEmpty(authorField.getText())) {
                    showError("Tên sách và tác giả không được để trống!");
                    return null;
                }
                Book b = book != null ? book : new Book();
                b.setIsbn(isbnField.getText().trim());
                b.setTitle(titleField.getText().trim());
                b.setAuthor(authorField.getText().trim());
                b.setPublisher(publisherField.getText().trim());
                try { b.setYear(Integer.parseInt(yearField.getText().trim())); } catch (Exception e) { b.setYear(2024); }
                if (catCombo.getValue() != null) b.setCategoryId(catCombo.getValue().getId());
                b.setQuantity(qtySpinner.getValue());
                if (book == null) b.setAvailable(qtySpinner.getValue());
                b.setDescription(descArea.getText().trim());
                b.setContent(contentArea.getText().trim());
                return b;
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(b -> {
            boolean success;
            if (book == null) {
                success = bookDAO.insert(b);
            } else {
                success = bookDAO.update(b);
            }
            if (success) {
                loadData();
                showInfo(book == null ? "Thêm sách thành công!" : "Cập nhật sách thành công!");
            } else {
                showError("Thao tác thất bại!");
            }
        });
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu tệp CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("books.csv");
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                CSVHelper.exportBooks(file, bookDAO.getAll());
                showInfo("Đã xuất danh sách sách ra tệp: " + file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Có lỗi xảy ra khi xuất tệp CSV: " + ex.getMessage());
            }
        }
    }

    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                List<Book> importedBooks = CSVHelper.importBooks(file);
                int count = 0;
                for (Book b : importedBooks) {
                    if (bookDAO.insert(b)) count++;
                }
                loadData();
                showInfo("Đã nhập thành công " + count + " sách từ tệp CSV!");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Có lỗi xảy ra khi đọc tệp CSV: " + ex.getMessage() + "\nVui lòng đảm bảo cấu trúc cột hợp lệ.");
            }
        }
    }
}
