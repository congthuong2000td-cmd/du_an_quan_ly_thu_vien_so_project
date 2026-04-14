package com.library.ui.panels.reader;

import com.library.dao.FavoriteDAO;
import com.library.model.Favorite;
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

public class ReaderFavoritesPanel extends VBox {
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final User currentUser;
    private TableView<Favorite> table;
    private ObservableList<Favorite> data;

    public ReaderFavoritesPanel(User user) {
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

        Label titleLabel = new Label("💖 Sách Yêu Thích");
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

        TableColumn<Favorite, String> bookCol = new TableColumn<>("Tên sách");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setMinWidth(250);

        TableColumn<Favorite, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("bookAuthor"));
        authorCol.setMinWidth(150);

        TableColumn<Favorite, String> categoryCol = new TableColumn<>("Thể loại");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("bookCategory"));
        categoryCol.setMinWidth(120);

        TableColumn<Favorite, String> dateCol = new TableColumn<>("Ngày lưu");
        dateCol.setCellValueFactory(d -> {
            if (d.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(DateUtils.formatDate(d.getValue().getCreatedAt().toLocalDate()));
            }
            return new SimpleStringProperty("—");
        });
        dateCol.setMinWidth(120);

        TableColumn<Favorite, Void> actionCol = new TableColumn<>("Hành động");
        actionCol.setMinWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("❌ Bỏ lưu");
            {
                removeBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #f38ba8; -fx-padding: 4 12; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    Favorite f = getTableView().getItems().get(getIndex());
                    if (favoriteDAO.toggleFavorite(currentUser.getId(), f.getBookId())) {
                        loadData();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(removeBtn);
            }
        });

        table.getColumns().addAll(bookCol, authorCol, categoryCol, dateCol, actionCol);

        // Empty state
        table.setPlaceholder(new Label("📭 Bạn chưa yêu thích cuốn sách nào.\nHãy quay lại Kho sách để xem thêm!"));

        getChildren().addAll(header, table);
    }

    private void loadData() {
        List<Favorite> favorites = favoriteDAO.getByUser(currentUser.getId());
        data = FXCollections.observableArrayList(favorites);
        table.setItems(data);
    }
}
