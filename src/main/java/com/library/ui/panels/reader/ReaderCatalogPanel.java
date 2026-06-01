package com.library.ui.panels.reader;

import com.library.service.CartService;

import com.library.dao.BookDAO;
import com.library.dao.BorrowDAO;
import com.library.dao.CategoryDAO;
import com.library.dao.ReaderDAO;
import com.library.dao.ReservationDAO;
import com.library.dao.ReviewDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Category;
import com.library.model.Reader;
import com.library.model.Reservation;
import com.library.model.User;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class ReaderCatalogPanel extends VBox {
    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final User currentUser;
    private FlowPane booksGrid;
    private TextField searchField;
    private ComboBox<String> categoryFilter;

    public ReaderCatalogPanel(User user) {
        this.currentUser = user;
        setSpacing(0);
        setPadding(new Insets(0));
        buildUI();
        loadBooks();
    }

    private void buildUI() {
        // Header
        HBox header = new HBox(15);
        header.getStyleClass().add("panel-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));

        Label titleLabel = new Label("📚 Kho Sách Thư Viện");
        titleLabel.getStyleClass().add("panel-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search
        searchField = new TextField();
        searchField.setPromptText("🔍 Tìm kiếm sách...");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(36);
        searchField.textProperty().addListener((obs, o, n) -> loadBooks());

        // Category filter
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("Tất cả thể loại");
        for (Category cat : categoryDAO.getAll()) {
            categoryFilter.getItems().add(cat.getName());
        }
        categoryFilter.setValue("Tất cả thể loại");
        categoryFilter.setOnAction(e -> loadBooks());

        header.getChildren().addAll(titleLabel, spacer, searchField, categoryFilter);

        // Books grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        booksGrid = new FlowPane();
        booksGrid.setPadding(new Insets(20));
        booksGrid.setHgap(20);
        booksGrid.setVgap(20);
        booksGrid.setAlignment(Pos.TOP_LEFT);
        scrollPane.setContent(booksGrid);

        getChildren().addAll(header, scrollPane);
    }

    private void loadBooks() {
        booksGrid.getChildren().clear();
        String searchText = searchField.getText().trim().toLowerCase();
        String category = categoryFilter.getValue();

        List<Book> allBooks = bookDAO.getAll();

        for (Book book : allBooks) {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean match = book.getTitle().toLowerCase().contains(searchText)
                        || book.getAuthor().toLowerCase().contains(searchText)
                        || (book.getIsbn() != null && book.getIsbn().contains(searchText));
                if (!match) continue;
            }
            // Category filter
            if (!"Tất cả thể loại".equals(category)) {
                if (book.getCategoryName() == null || !book.getCategoryName().equals(category)) continue;
            }

            booksGrid.getChildren().add(createBookCard(book));
        }

        if (booksGrid.getChildren().isEmpty()) {
            Label emptyLabel = new Label("📭 Không tìm thấy sách nào");
            emptyLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 16px;");
            booksGrid.getChildren().add(emptyLabel);
        }
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setMinHeight(200);
        card.setPadding(new Insets(18));
        card.setStyle("""
            -fx-background-color: #1e1e2e;
            -fx-background-radius: 12;
            -fx-border-color: #313244;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
            -fx-cursor: hand;
        """);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("""
            -fx-background-color: #262637;
            -fx-background-radius: 12;
            -fx-border-color: #89b4fa;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(137,180,250,0.3), 12, 0, 0, 3);
            -fx-cursor: hand;
        """));
        card.setOnMouseExited(e -> card.setStyle("""
            -fx-background-color: #1e1e2e;
            -fx-background-radius: 12;
            -fx-border-color: #313244;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
            -fx-cursor: hand;
        """));

        // Category badge
        Label catBadge = new Label(book.getCategoryName() != null ? book.getCategoryName() : "Khác");
        catBadge.setStyle("-fx-background-color: #313244; -fx-text-fill: #89b4fa; -fx-padding: 3 10; " +
                "-fx-background-radius: 10; -fx-font-size: 11px;");

        // Title
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        // Author
        Label authorLabel = new Label("✍️ " + book.getAuthor());
        authorLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 13px;");

        // Publisher & Year
        Label publisherLabel = new Label("📅 " + book.getYear() + (book.getPublisher() != null ? " | " + book.getPublisher() : ""));
        publisherLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12px;");

        // Rating
        double avgRating = reviewDAO.getAverageRating(book.getId());
        Label ratingLabel;
        if (avgRating > 0) {
            ratingLabel = new Label(String.format("⭐ %.1f", avgRating));
            ratingLabel.setStyle("-fx-text-fill: #f9e2af; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            ratingLabel = new Label("⭐ Chưa có");
            ratingLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12px;");
        }

        // Availability
        Label availLabel;
        if (book.getAvailable() > 0) {
            availLabel = new Label("✅ Còn " + book.getAvailable() + "/" + book.getQuantity() + " cuốn");
            availLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            availLabel = new Label("❌ Hết sách");
            availLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        // Online badge if has content
        HBox badges = new HBox(8, catBadge);
        if (book.getContent() != null && !book.getContent().isEmpty()) {
            Label onlineBadge = new Label("📖 Đọc online");
            onlineBadge.setStyle("-fx-background-color: #1e3a2e; -fx-text-fill: #a6e3a1; -fx-padding: 3 10; " +
                    "-fx-background-radius: 10; -fx-font-size: 11px;");
            badges.getChildren().add(onlineBadge);
        }

        // Description
        Label descLabel = new Label(book.getDescription() != null ? book.getDescription() : "");
        descLabel.setStyle("-fx-text-fill: #585b70; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Action buttons container
        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER);

        // Borrow button
        Button borrowBtn = new Button("Mượn ngay");
        borrowBtn.setMaxWidth(Double.MAX_VALUE);
        borrowBtn.setStyle("-fx-background-color: #f9e2af; -fx-text-fill: #11111b; -fx-cursor: hand; -fx-font-weight: bold;");
        HBox.setHgrow(borrowBtn, Priority.ALWAYS);

        // Reserve button
        Button reserveBtn = new Button("Đặt sách");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-cursor: hand; -fx-font-weight: bold;");
        HBox.setHgrow(reserveBtn, Priority.ALWAYS);

        // Cart button
        Button cartBtn = new Button("🛒 Giỏ hàng");
        cartBtn.setMaxWidth(Double.MAX_VALUE);
        cartBtn.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #11111b; -fx-cursor: hand; -fx-font-weight: bold;");
        HBox.setHgrow(cartBtn, Priority.ALWAYS);

        if (book.getAvailable() > 0) {
            if (reservationDAO.hasPendingReservation(currentUser.getId(), book.getId())) {
                reserveBtn.setText("⏳ Đã đặt - Chờ duyệt");
                reserveBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #fab387; " +
                        "-fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px;");
                reserveBtn.setDisable(true);
            } else {
                reserveBtn.setOnAction(e -> handleReserve(book));
            }
            borrowBtn.setOnAction(e -> handleBorrowDirectly(book));
            
            cartBtn.setOnAction(e -> {
                if (CartService.getInstance().containsBook(book.getId())) {
                    new Alert(Alert.AlertType.INFORMATION, "Sách đã có trong giỏ hàng!").show();
                } else {
                    CartService.getInstance().addBook(book);
                    cartBtn.setText("✅ Đã thêm");
                    cartBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #a6e3a1; -fx-cursor: hand; -fx-font-weight: bold;");
                    cartBtn.setDisable(true);
                }
            });
            
            if (CartService.getInstance().containsBook(book.getId())) {
                cartBtn.setText("✅ Đã thêm");
                cartBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #a6e3a1; -fx-cursor: hand; -fx-font-weight: bold;");
                cartBtn.setDisable(true);
            }

            actionsRow.getChildren().addAll(borrowBtn, cartBtn); // Thay thế reserveBtn bằng cartBtn để không quá chật, hoặc gom nhóm
        } else {
            reserveBtn.setText("Hết sách");
            reserveBtn.setDisable(true);
            borrowBtn.setDisable(true);
            cartBtn.setDisable(true);
            actionsRow.getChildren().addAll(reserveBtn);
        }

        // Details button
        Button detailsBtn = new Button("Chi tiết / Xem Đánh giá");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #89b4fa; -fx-border-color: #89b4fa; -fx-border-radius: 8; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> {
            new ReaderBookDetailsDialog(book, currentUser).showAndWait();
            // Refresh to update stars
            loadBooks();
        });

        // Cover image
        ImageView coverView = new ImageView();
        try {
            coverView.setImage(new Image(getClass().getResourceAsStream("/images/default_cover.png")));
            coverView.setFitWidth(80);
            coverView.setFitHeight(115);
            coverView.setPreserveRatio(false); // To ensure consistent dimensions
            // Optional rounding using CSS or clip would be nice but not required
            coverView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        } catch (Exception e) {}

        VBox textBox = new VBox(4, titleLabel, authorLabel, publisherLabel, ratingLabel, descLabel);
        HBox contentBox = new HBox(12, coverView, textBox);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        card.getChildren().addAll(badges, contentBox, spacer, availLabel, actionsRow, detailsBtn);
        return card;
    }

    private void handleBorrowDirectly(Book book) {
        String readerName = currentUser.getFullName();
        Reader reader = readerDAO.getByName(readerName);
        
        // Auto-create Reader profile if not exist to allow seamless direct borrow
        if (reader == null) {
            reader = new Reader();
            reader.setName(readerName);
            reader.setCode("R" + System.currentTimeMillis());
            reader.setEmail(currentUser.getUsername() + "@library.com");
            reader.setPhone("0000000000");
            reader.setType("Thường");
            readerDAO.insert(reader);
            reader = readerDAO.getByName(readerName);
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận mượn sách");
        confirm.setHeaderText("Bạn muốn trực tiếp mượn sách: " + book.getTitle() + "?");
        confirm.setContentText("Sách sẽ được tự động thêm vào giỏ Sách Đang Mượn với thời hạn mặc định là 14 ngày. Hãy đến thư viện để lấy sách vật lý nhé!");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            BorrowRecord record = new BorrowRecord();
            record.setReaderId(reader.getId());
            record.setBookId(book.getId());
            record.setBorrowDate(java.time.LocalDate.now());
            record.setDueDate(java.time.LocalDate.now().plusDays(14));
            record.setStatus("BORROWING");

            if (borrowDAO.borrowBook(record.getReaderId(), record.getBookId(), record.getBorrowDate(), record.getDueDate())) {
                book.setAvailable(book.getAvailable() - 1);
                bookDAO.update(book);
                loadBooks();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION, "✅ Đã mượn thành công! Hãy vào mục 'Sách đang mượn' để xem hạn trả.", ButtonType.OK);
                success.showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Lỗi hệ thống khi tạo phiếu mượn.", ButtonType.OK).show();
            }
        }
    }

    private void handleReserve(Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đặt sách");
        confirm.setHeaderText("Đặt sách: " + book.getTitle());
        confirm.setContentText("Yêu cầu đặt sách sẽ được gửi đến thủ thư để xử lý. Bạn có muốn tiếp tục?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Reservation reservation = new Reservation(currentUser.getId(), book.getId());
                if (reservationDAO.insert(reservation)) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Thành công");
                    success.setHeaderText(null);
                    success.setContentText("✅ Đã gửi yêu cầu đặt sách \"" + book.getTitle() + "\"!\nVui lòng chờ thủ thư phê duyệt.");
                    success.showAndWait();
                    loadBooks(); // Refresh
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Lỗi");
                    error.setHeaderText(null);
                    error.setContentText("❌ Không thể đặt sách. Vui lòng thử lại!");
                    error.showAndWait();
                }
            }
        });
    }
}
