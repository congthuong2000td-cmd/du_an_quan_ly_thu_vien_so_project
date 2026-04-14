package com.library.ui.panels.reader;

import com.library.dao.FavoriteDAO;
import com.library.dao.ReviewDAO;
import com.library.model.Book;
import com.library.model.Review;
import com.library.model.User;
import com.library.util.DateUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class ReaderBookDetailsDialog extends Dialog<Void> {
    private final Book book;
    private final User currentUser;
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private VBox reviewsContainer;

    public ReaderBookDetailsDialog(Book book, User currentUser) {
        this.book = book;
        this.currentUser = currentUser;

        setTitle(book.getTitle());
        setHeaderText(null);
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        getDialogPane().getStyleClass().add("dialog-pane");

        // Tweak width
        getDialogPane().setPrefWidth(600);

        buildUI();
    }

    private void buildUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(10));

        // Top info
        HBox topInfo = new HBox(20);
        
        VBox infoBox = new VBox(5);
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #cdd6f4;");
        titleLabel.setWrapText(true);
        Label authorLabel = new Label("Tác giả: " + book.getAuthor());
        authorLabel.setStyle("-fx-text-fill: #a6adc8;");
        
        double avgRating = reviewDAO.getAverageRating(book.getId());
        int count = reviewDAO.getReviewCount(book.getId());
        Label ratingLabel = new Label(String.format("⭐ %.1f (%d đánh giá)", avgRating, count));
        ratingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f9e2af;");

        // Favorite Button
        Button favBtn = new Button();
        boolean isFav = favoriteDAO.isFavorite(currentUser.getId(), book.getId());
        updateFavBtnStyle(favBtn, isFav);
        favBtn.setOnAction(e -> {
            boolean currentFav = favoriteDAO.isFavorite(currentUser.getId(), book.getId());
            favoriteDAO.toggleFavorite(currentUser.getId(), book.getId());
            updateFavBtnStyle(favBtn, !currentFav);
        });

        HBox actionsRow = new HBox(15, ratingLabel, favBtn);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        
        infoBox.getChildren().addAll(titleLabel, authorLabel, new Label("Sẵn có: " + book.getAvailable() + "/" + book.getQuantity()), actionsRow);

        topInfo.getChildren().addAll(infoBox);

        // Reviews section
        Label revTitle = new Label("Nhận xét từ độc giả");
        revTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #cdd6f4;");

        reviewsContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(reviewsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        loadReviews();

        // Add review section
        VBox addReviewBox = new VBox(10);
        addReviewBox.setPadding(new Insets(15));
        addReviewBox.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 8;");
        
        if (reviewDAO.hasReviewed(currentUser.getId(), book.getId())) {
            Label doneLabel = new Label("Bạn đã nhận xét cuốn sách này.");
            doneLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-style: italic;");
            addReviewBox.getChildren().add(doneLabel);
        } else {
            Label inputLbl = new Label("Viết nhận xét của bạn:");
            HBox starsBox = new HBox(5);
            ComboBox<Integer> ratingCombo = new ComboBox<>();
            ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
            ratingCombo.setValue(5);
            starsBox.getChildren().addAll(new Label("Đánh giá sao:"), ratingCombo);
            starsBox.setAlignment(Pos.CENTER_LEFT);

            TextArea commentArea = new TextArea();
            commentArea.setPrefRowCount(3);
            commentArea.setPromptText("Viết vài dòng chia sẻ về cuốn sách này...");

            Button submitBtn = new Button("Gửi nhận xét");
            submitBtn.getStyleClass().add("btn-primary");
            submitBtn.setOnAction(e -> {
                String c = commentArea.getText().trim();
                Review r = new Review();
                r.setUserId(currentUser.getId());
                r.setBookId(book.getId());
                r.setRating(ratingCombo.getValue());
                r.setComment(c);

                if (reviewDAO.addReview(r)) {
                    loadReviews(); // reload
                    addReviewBox.getChildren().clear();
                    Label doneLabel = new Label("Cảm ơn bạn đã đóng góp nhận xét!");
                    doneLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-style: italic;");
                    addReviewBox.getChildren().add(doneLabel);
                }
            });
            addReviewBox.getChildren().addAll(inputLbl, starsBox, commentArea, submitBtn);
        }

        root.getChildren().addAll(topInfo, new Separator(), revTitle, scrollPane, addReviewBox);
        getDialogPane().setContent(root);
    }

    private void updateFavBtnStyle(Button btn, boolean isFav) {
        if (isFav) {
            btn.setText("💖 Đã Yêu thích");
            btn.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-background-radius: 6;");
        } else {
            btn.setText("🤍 Yêu thích");
            btn.setStyle("-fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-font-weight: bold; -fx-background-radius: 6;");
        }
    }

    private void loadReviews() {
        reviewsContainer.getChildren().clear();
        List<Review> list = reviewDAO.getByBook(book.getId());
        if (list.isEmpty()) {
            Label emptyLbl = new Label("Chưa có nhận xét nào.");
            emptyLbl.setStyle("-fx-text-fill: #a6adc8; -fx-font-style: italic;");
            reviewsContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Review r : list) {
            VBox box = new VBox(5);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color: #313244; -fx-background-radius: 8;");
            
            HBox header = new HBox(10);
            Label nameLbl = new Label(r.getUserName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #89b4fa;");
            Label starsLbl = new Label("⭐".repeat(r.getRating()));
            starsLbl.setStyle("-fx-text-fill: #f9e2af;");
            
            String date = r.getCreatedAt() != null ? DateUtils.formatDate(r.getCreatedAt().toLocalDate()) : "";
            Label dateLbl = new Label(date);
            dateLbl.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 11px;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            header.getChildren().addAll(nameLbl, starsLbl, spacer, dateLbl);
            
            Label commentLbl = new Label(r.getComment());
            commentLbl.setWrapText(true);
            commentLbl.setStyle("-fx-text-fill: #cdd6f4;");

            box.getChildren().addAll(header, commentLbl);
            reviewsContainer.getChildren().add(box);
        }
    }
}
