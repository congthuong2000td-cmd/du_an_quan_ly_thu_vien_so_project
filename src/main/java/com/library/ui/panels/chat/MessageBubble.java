package com.library.ui.panels.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.library.model.Message;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MessageBubble extends HBox {
    private final Message message;
    private final boolean isMine;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public MessageBubble(Message message, int currentUserId) {
        this.message = message;
        this.isMine = message.getSenderId() == currentUserId;
        
        buildUI();
    }

    private void buildUI() {
        setPadding(new Insets(5, 10, 5, 10));
        setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubbleContainer = new VBox(2);
        bubbleContainer.setMaxWidth(400);
        bubbleContainer.setPadding(new Insets(8, 12, 8, 12));
        bubbleContainer.getStyleClass().add(isMine ? "chat-bubble-mine" : "chat-bubble-other");
        
        // Sender name if not mine
        if (!isMine) {
            Label nameLabel = new Label(message.getSenderName());
            nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #b4befe; -fx-font-weight: bold;");
            bubbleContainer.getChildren().add(nameLabel);
        }

        Text text = new Text(message.getContent());
        text.getStyleClass().add("chat-text");
        text.setWrappingWidth(350);
        
        LocalDateTime sentAt = message.getSentAt() != null ? message.getSentAt() : LocalDateTime.now();
        Label timeLabel = new Label(sentAt.format(timeFormatter));
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #9399b2;");
        
        HBox footer = new HBox(5);
        footer.setAlignment(Pos.BOTTOM_RIGHT);
        footer.getChildren().add(timeLabel);
        
        bubbleContainer.getChildren().addAll(text, footer);
        
        getChildren().add(bubbleContainer);
    }
}
