package com.library.ui.panels.chat;

import com.library.model.Conversation;
import com.library.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ConversationCell extends ListCell<Conversation> {
    private final int currentUserId;

    public ConversationCell(int currentUserId) {
        this.currentUserId = currentUserId;
        getStyleClass().add("conversation-cell");
    }

    @Override
    protected void updateItem(Conversation item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            HBox container = new HBox(12);
            container.setPadding(new Insets(10));
            container.setAlignment(Pos.CENTER_LEFT);

            Circle avatar = new Circle(18);
            avatar.getStyleClass().add("chat-avatar");
            
            VBox textContainer = new VBox(2);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            
            Label nameLabel = new Label(item.getDisplayName(currentUserId));
            nameLabel.getStyleClass().add("conv-name");
            
            Label lastMsgLabel = new Label();
            lastMsgLabel.getStyleClass().add("conv-last-msg");
            if (item.getLastMessage() != null) {
                lastMsgLabel.setText(item.getLastMessage().getContent());
            } else {
                lastMsgLabel.setText("Chưa có tin nhắn");
            }
            
            textContainer.getChildren().addAll(nameLabel, lastMsgLabel);
            
            VBox metaContainer = new VBox(5);
            metaContainer.setAlignment(Pos.CENTER_RIGHT);
            
            if (item.getUnreadCount() > 0) {
                Label badge = new Label(String.valueOf(item.getUnreadCount()));
                badge.getStyleClass().add("chat-badge-small");
                metaContainer.getChildren().add(badge);
            }
            
            container.getChildren().addAll(avatar, textContainer, metaContainer);
            setGraphic(container);
        }
    }
}
