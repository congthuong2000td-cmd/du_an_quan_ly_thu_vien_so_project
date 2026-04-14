package com.library.ui.panels.reader;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.User;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.util.List;

public class ReaderReadOnlinePanel extends VBox {
    private final BookDAO bookDAO = new BookDAO();
    private final User currentUser;
    private FlowPane booksGrid;
    private VBox readingView;
    private VBox listView;

    public ReaderReadOnlinePanel(User user) {
        this.currentUser = user;
        setSpacing(0);
        buildUI();
    }

    private void buildUI() {
        // ========== LIST VIEW ==========
        listView = new VBox(0);
        VBox.setVgrow(listView, Priority.ALWAYS);

        HBox header = new HBox(15);
        header.getStyleClass().add("panel-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));

        Label titleLabel = new Label("📖 Đọc Sách Online");
        titleLabel.getStyleClass().add("panel-title");

        Label subtitleLabel = new Label("Chọn một cuốn sách để bắt đầu đọc");
        subtitleLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 13px;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

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

        listView.getChildren().addAll(header, scrollPane);

        // ========== READING VIEW ==========
        readingView = new VBox();
        readingView.setVisible(false);
        readingView.setManaged(false);
        VBox.setVgrow(readingView, Priority.ALWAYS);

        getChildren().addAll(listView, readingView);
        loadOnlineBooks();
    }

    private void loadOnlineBooks() {
        booksGrid.getChildren().clear();
        List<Book> allBooks = bookDAO.getAll();

        boolean hasAny = false;
        for (Book book : allBooks) {
            if (book.getContent() != null && !book.getContent().isEmpty()) {
                booksGrid.getChildren().add(createBookCard(book));
                hasAny = true;
            }
        }

        if (!hasAny) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            Label emptyIcon = new Label("📚");
            emptyIcon.setStyle("-fx-font-size: 48px;");
            Label emptyLabel = new Label("Chưa có sách nào được đăng tải online");
            emptyLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 16px;");
            emptyBox.getChildren().addAll(emptyIcon, emptyLabel);
            booksGrid.getChildren().add(emptyBox);
        }
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(12);
        card.setPrefWidth(300);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #1e1e2e, #262637);
            -fx-background-radius: 14;
            -fx-border-color: #313244;
            -fx-border-radius: 14;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
            -fx-cursor: hand;
        """);

        card.setOnMouseEntered(e -> card.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #262637, #2e2e42);
            -fx-background-radius: 14;
            -fx-border-color: #a6e3a1;
            -fx-border-radius: 14;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(166,227,161,0.3), 14, 0, 0, 3);
            -fx-cursor: hand;
        """));
        card.setOnMouseExited(e -> card.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #1e1e2e, #262637);
            -fx-background-radius: 14;
            -fx-border-color: #313244;
            -fx-border-radius: 14;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
            -fx-cursor: hand;
        """));

        // Online tag
        Label onlineTag = new Label("📖 Có bản Online");
        onlineTag.setStyle("-fx-background-color: #1e3a2e; -fx-text-fill: #a6e3a1; -fx-padding: 4 12; " +
                "-fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        Label authorLabel = new Label("✍️ " + book.getAuthor());
        authorLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 13px;");

        Label descLabel = new Label(book.getDescription() != null ? book.getDescription() : "");
        descLabel.setStyle("-fx-text-fill: #585b70; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(36);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button readBtn = new Button("📖 Bắt đầu đọc");
        readBtn.setMaxWidth(Double.MAX_VALUE);
        readBtn.setStyle("-fx-background-color: linear-gradient(to right, #a6e3a1, #94e2d5); " +
                "-fx-text-fill: #1e1e2e; -fx-padding: 10 16; -fx-background-radius: 8; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        readBtn.setOnAction(e -> openReader(book));

        card.getChildren().addAll(onlineTag, titleLabel, authorLabel, descLabel, spacer, readBtn);
        return card;
    }

    private void openReader(Book book) {
        listView.setVisible(false);
        listView.setManaged(false);
        readingView.setVisible(true);
        readingView.setManaged(true);
        readingView.getChildren().clear();

        // Reading header
        HBox readHeader = new HBox(15);
        readHeader.setAlignment(Pos.CENTER_LEFT);
        readHeader.setPadding(new Insets(15, 25, 15, 25));
        readHeader.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 0 0 1 0;");

        Button backBtn = new Button("← Quay lại");
        backBtn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-padding: 6 16; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        backBtn.setOnAction(e -> closeReader());

        Label readTitle = new Label("📖 " + book.getTitle());
        readTitle.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label readAuthor = new Label("— " + book.getAuthor());
        readAuthor.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 14px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Font size control
        Spinner<Integer> fontSpinner = new Spinner<>(12, 28, 18, 2);
        fontSpinner.setPrefWidth(80);
        fontSpinner.setStyle("-fx-font-size: 12px;");
        Label fontLabel = new Label("Cỡ chữ:");
        fontLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

        readHeader.getChildren().addAll(backBtn, readTitle, readAuthor, sp, fontLabel, fontSpinner);

        // Reading content
        ScrollPane readScroll = new ScrollPane();
        readScroll.setFitToWidth(true);
        readScroll.setStyle("-fx-background: #181825; -fx-background-color: #181825;");
        VBox.setVgrow(readScroll, Priority.ALWAYS);

        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(40, 80, 40, 80));
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setStyle("-fx-background-color: #1e1e2e;");
        contentBox.setMaxWidth(800);

        TextFlow textFlow = new TextFlow();
        textFlow.setLineSpacing(8);

        Text contentText = new Text(book.getContent());
        contentText.setStyle("-fx-fill: #cdd6f4; -fx-font-size: 18px;");
        contentText.setFont(Font.font("serif", 18));

        // Font size change
        fontSpinner.valueProperty().addListener((obs, o, n) -> {
            contentText.setFont(Font.font("serif", n));
            contentText.setStyle("-fx-fill: #cdd6f4; -fx-font-size: " + n + "px;");
        });

        textFlow.getChildren().add(contentText);

        // Center content
        HBox centerWrapper = new HBox(contentBox);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #181825;");
        contentBox.getChildren().add(textFlow);
        readScroll.setContent(centerWrapper);

        // Footer
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 25, 10, 25));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 1 0 0 0;");
        Label footerText = new Label("📖 Đang đọc: " + book.getTitle() + " — " + book.getAuthor());
        footerText.setStyle("-fx-text-fill: #585b70; -fx-font-size: 12px;");
        footer.getChildren().add(footerText);

        readingView.getChildren().addAll(readHeader, readScroll, footer);
    }

    private void closeReader() {
        readingView.setVisible(false);
        readingView.setManaged(false);
        listView.setVisible(true);
        listView.setManaged(true);
    }
}
