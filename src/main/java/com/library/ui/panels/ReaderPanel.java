package com.library.ui.panels;

import com.library.dao.ReaderDAO;
import com.library.model.Reader;
import com.library.util.Constants;
import com.library.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.Optional;
import java.io.File;
import java.util.List;
import javafx.stage.FileChooser;
import com.library.util.CSVHelper;

public class ReaderPanel extends VBox {
    private final ReaderDAO readerDAO = new ReaderDAO();
    private TableView<Reader> table;
    private ObservableList<Reader> readerList;
    private TextField searchField;

    public ReaderPanel() {
        setSpacing(16);
        buildUI();
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        Label title = new Label("Quản lý Độc giả");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Quản lý thông tin độc giả thư viện");
        subtitle.getStyleClass().add("page-subtitle");

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.getStyleClass().add("search-box");
        searchField.setPromptText("\uD83D\uDD0D Tìm kiếm độc giả...");
        searchField.textProperty().addListener((obs, o, n) -> handleSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button addBtn = new Button("+ Thêm độc giả");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showReaderDialog(null));

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

        table = new TableView<>();
        table.setPlaceholder(new Label("Chưa có độc giả nào"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Reader, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Reader, String> codeCol = new TableColumn<>("Mã ĐG");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<Reader, String> nameCol = new TableColumn<>("Họ tên");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Reader, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<Reader, String> phoneCol = new TableColumn<>("SĐT");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(110);

        TableColumn<Reader, String> typeCol = new TableColumn<>("Loại");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<Reader, Void> actionCol = new TableColumn<>("Thao tác");
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
                editBtn.setOnAction(e -> showReaderDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, codeCol, nameCol, emailCol, phoneCol, typeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Label infoLabel = new Label();
        infoLabel.getStyleClass().add("page-subtitle");
        readerList = FXCollections.observableArrayList();
        readerList.addListener((javafx.collections.ListChangeListener<Reader>) c ->
            infoLabel.setText("Tổng: " + readerList.size() + " độc giả"));

        getChildren().addAll(new VBox(4, title, subtitle), toolbar, table, infoLabel);
    }

    private void loadData() {
        readerList.setAll(readerDAO.getAll());
        table.setItems(readerList);
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) loadData();
        else readerList.setAll(readerDAO.search(keyword));
    }

    private void handleDelete(Reader reader) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa độc giả: " + reader.getName());
        confirm.setContentText("Bạn có chắc chắn muốn xóa?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (readerDAO.delete(reader.getId())) {
                loadData();
            } else {
                new Alert(Alert.AlertType.ERROR, "Không thể xóa. Độc giả có thể đang mượn sách.", ButtonType.OK).showAndWait();
            }
        }
    }

    private void showReaderDialog(Reader reader) {
        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle(reader == null ? "Thêm độc giả mới" : "Sửa thông tin độc giả");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20)); grid.setPrefWidth(450);

        TextField codeField = new TextField(reader != null ? reader.getCode() : readerDAO.generateNextCode());
        TextField nameField = new TextField(reader != null ? reader.getName() : "");
        nameField.setPromptText("Họ và tên");
        TextField emailField = new TextField(reader != null ? reader.getEmail() : "");
        emailField.setPromptText("email@example.com");
        TextField phoneField = new TextField(reader != null ? reader.getPhone() : "");
        phoneField.setPromptText("0901234567");
        TextField addressField = new TextField(reader != null ? reader.getAddress() : "");
        addressField.setPromptText("Địa chỉ");
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
            Constants.TYPE_STUDENT, Constants.TYPE_LECTURER, Constants.TYPE_OTHER));
        typeCombo.setValue(reader != null ? reader.getType() : Constants.TYPE_STUDENT);
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        int row = 0;
        grid.addRow(row++, new Label("Mã ĐG:"), codeField);
        grid.addRow(row++, new Label("Họ tên:"), nameField);
        grid.addRow(row++, new Label("Email:"), emailField);
        grid.addRow(row++, new Label("SĐT:"), phoneField);
        grid.addRow(row++, new Label("Địa chỉ:"), addressField);
        grid.addRow(row++, new Label("Loại:"), typeCombo);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                if (ValidationUtils.isNullOrEmpty(nameField.getText())) {
                    new Alert(Alert.AlertType.ERROR, "Họ tên không được để trống!", ButtonType.OK).showAndWait();
                    return null;
                }
                Reader r = reader != null ? reader : new Reader();
                r.setCode(codeField.getText().trim());
                r.setName(nameField.getText().trim());
                r.setEmail(emailField.getText().trim());
                r.setPhone(phoneField.getText().trim());
                r.setAddress(addressField.getText().trim());
                r.setType(typeCombo.getValue());
                return r;
            }
            return null;
        });

        Optional<Reader> result = dialog.showAndWait();
        result.ifPresent(r -> {
            boolean success = reader == null ? readerDAO.insert(r) : readerDAO.update(r);
            if (success) loadData();
            else new Alert(Alert.AlertType.ERROR, "Thao tác thất bại!", ButtonType.OK).showAndWait();
        });
    }

    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu tệp CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("readers.csv");
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                CSVHelper.exportReaders(file, readerDAO.getAll());
                new Alert(Alert.AlertType.INFORMATION, "Đã xuất danh sách độc giả ra tệp: " + file.getName(), ButtonType.OK).showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Có lỗi xảy ra khi xuất tệp CSV: " + ex.getMessage(), ButtonType.OK).showAndWait();
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
                List<Reader> importedReaders = CSVHelper.importReaders(file);
                int count = 0;
                for (Reader r : importedReaders) {
                    if (readerDAO.insert(r)) count++;
                }
                loadData();
                new Alert(Alert.AlertType.INFORMATION, "Đã nhập thành công " + count + " độc giả từ tệp CSV!", ButtonType.OK).showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Có lỗi xảy ra khi đọc tệp CSV: " + ex.getMessage() + "\nVui lòng đảm bảo cấu trúc cột hợp lệ.", ButtonType.OK).showAndWait();
            }
        }
    }
}
