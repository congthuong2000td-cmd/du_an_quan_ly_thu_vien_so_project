package com.library.ui.panels.admin;

import com.library.dao.CategoryDAO;
import com.library.model.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.Optional;

public class CategoryPanel extends VBox {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoryList;
    private TableView<Category> table;
    private TextField searchField;

    public CategoryPanel() {
        setSpacing(16);
        setPadding(new Insets(20));

        Label title = new Label("Quản lý Thể loại sách");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Thêm, sửa, xóa danh mục các thể loại sách trong hệ thống");
        subtitle.getStyleClass().add("page-subtitle");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("➕ Thêm Thể loại");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showCategoryDialog(null));

        searchField = new TextField();
        searchField.setPromptText("🔍 Tìm theo tên thể loại...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldV, newV) -> handleSearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(addBtn, searchField, spacer);

        // Table
        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Category, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Category, String> nameCol = new TableColumn<>("Tên Tên Thể loại");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);

        TableColumn<Category, String> descCol = new TableColumn<>("Mô tả");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);

        TableColumn<Category, Void> actionCol = new TableColumn<>("Thao tác");
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
                editBtn.setOnAction(e -> showCategoryDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, descCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Label infoLabel = new Label();
        infoLabel.getStyleClass().add("page-subtitle");
        categoryList = FXCollections.observableArrayList();
        categoryList.addListener((javafx.collections.ListChangeListener<Category>) c ->
            infoLabel.setText("Tổng: " + categoryList.size() + " thể loại"));

        getChildren().addAll(new VBox(4, title, subtitle), toolbar, table, infoLabel);
        
        loadData();
    }

    private void loadData() {
        categoryList.setAll(categoryDAO.getAll());
        table.setItems(categoryList);
    }

    private void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadData();
        } else {
            categoryList.setAll(categoryDAO.getAll().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(kw))
                .toList());
        }
    }

    private void handleDelete(Category category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa Thể loại: " + category.getName());
        confirm.setContentText("Bạn có chắc chắn muốn xóa thể loại này? Sách thuộc thể loại này có thể bị mất liên kết.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM categories WHERE id = ?";
            try (java.sql.PreparedStatement ps = com.library.dao.DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setInt(1, category.getId());
                if (ps.executeUpdate() > 0) {
                    loadData();
                    new Alert(Alert.AlertType.INFORMATION, "Xóa thành công!", ButtonType.OK).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showCategoryDialog(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(category == null ? "Thêm thể loại mới" : "Sửa thể loại");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(category != null ? category.getName() : "");
        nameField.setPromptText("Ví dụ: Khoa học viễn tưởng...");
        TextArea descArea = new TextArea(category != null ? category.getDescription() : "");
        descArea.setPromptText("Tùy chọn ghi chú...");
        descArea.setPrefRowCount(3);

        grid.addRow(0, new Label("Tên thể loại:"), nameField);
        grid.addRow(1, new Label("Mô tả:"), descArea);
        GridPane.setHgrow(nameField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                if (nameField.getText().trim().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Tên thể loại không được để trống!", ButtonType.OK).showAndWait();
                    return null;
                }
                Category c = category != null ? category : new Category();
                c.setName(nameField.getText().trim());
                c.setDescription(descArea.getText().trim());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            String sql;
            if (c.getId() == 0) {
                sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
            } else {
                sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
            }
            try (java.sql.PreparedStatement ps = com.library.dao.DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getDescription());
                if (c.getId() != 0) ps.setInt(3, c.getId());
                ps.executeUpdate();
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
