package com.library.service;

import com.library.dao.ConversationDAO;
import com.library.dao.MessageDAO;
import com.library.dao.ChatNotificationDAO;
import com.library.dao.UserDAO;
import com.library.model.Conversation;
import com.library.model.Message;
import com.library.model.User;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatService {
    private static ChatService instance;
    private final ConversationDAO conversationDAO = new ConversationDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final ChatNotificationDAO notificationDAO = new ChatNotificationDAO();
    private final UserDAO userDAO = new UserDAO();
    
    private User currentUser;
    private List<Consumer<Message>> messageListeners = new ArrayList<>();
    private List<Consumer<Integer>> notificationListeners = new ArrayList<>();
    
    private ScheduledService<Void> pollingService;
    private int lastProcessedMessageId = -1;

    private ChatService() {}

    public static ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public void start(User user) {
        this.currentUser = user;
        userDAO.updateStatus(user.getId(), "ONLINE");
        startPolling();
    }

    public void stop() {
        if (pollingService != null) {
            pollingService.cancel();
        }
        if (currentUser != null) {
            userDAO.updateStatus(currentUser.getId(), "OFFLINE");
        }
    }

    private void startPolling() {
        pollingService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        checkForNewMessages();
                        return null;
                    }
                };
            }
        };
        pollingService.setPeriod(Duration.seconds(3));
        pollingService.start();
    }

    private synchronized void checkForNewMessages() {
        if (currentUser == null) return;
        
        // Simple polling: check all conversations for unread messages sent by others
        List<Conversation> convs = conversationDAO.getUserConversations(currentUser.getId());
        boolean hasNew = false;
        
        for (Conversation conv : convs) {
            if (conv.getUnreadCount() > 0) {
                List<Message> messages = messageDAO.getMessagesByConversation(conv.getId());
                for (Message m : messages) {
                    if (m.getId() > lastProcessedMessageId && m.getSenderId() != currentUser.getId()) {
                        lastProcessedMessageId = Math.max(lastProcessedMessageId, m.getId());
                        Platform.runLater(() -> notifyMessageListeners(m));
                        hasNew = true;
                    }
                }
            }
        }
        
        if (hasNew) {
            int unreadTotal = notificationDAO.getUnreadCount(currentUser.getId());
            Platform.runLater(() -> notifyNotificationListeners(unreadTotal));
        }
    }

    public void sendMessage(Message message) {
        if (messageDAO.addMessage(message)) {
            // Add notification for other members
            List<User> members = conversationDAO.getConversationMembers(message.getConversationId());
            for (User member : members) {
                if (member.getId() != currentUser.getId()) {
                    com.library.model.ChatNotification note = new com.library.model.ChatNotification();
                    note.setUserId(member.getId());
                    note.setMessageId(message.getId());
                    notificationDAO.addNotification(note);
                }
            }
            notifyMessageListeners(message);
        }
    }

    public void addMessageListener(Consumer<Message> listener) {
        messageListeners.add(listener);
    }

    public void addNotificationListener(Consumer<Integer> listener) {
        notificationListeners.add(listener);
    }

    private void notifyMessageListeners(Message message) {
        for (Consumer<Message> listener : messageListeners) {
            listener.accept(message);
        }
    }

    private void notifyNotificationListeners(int count) {
        for (Consumer<Integer> listener : notificationListeners) {
            listener.accept(count);
        }
    }
    
    public List<Conversation> getConversations() {
        return conversationDAO.getUserConversations(currentUser.getId());
    }
    
    public List<Message> getChatHistory(int conversationId) {
        return messageDAO.getMessagesByConversation(conversationId);
    }
    
    public void markAsRead(int conversationId) {
        messageDAO.markAsSeen(conversationId, currentUser.getId());
        notificationDAO.markAsRead(currentUser.getId(), conversationId);
        int unreadTotal = notificationDAO.getUnreadCount(currentUser.getId());
        notifyNotificationListeners(unreadTotal);
    }
}
