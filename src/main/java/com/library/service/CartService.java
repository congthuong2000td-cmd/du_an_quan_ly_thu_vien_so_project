package com.library.service;

import com.library.model.Book;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static CartService instance;
    private final List<Book> cartItems;
    
    private CartService() {
        this.cartItems = new ArrayList<>();
    }
    
    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }
    
    public void addBook(Book book) {
        if (!containsBook(book.getId())) {
            cartItems.add(book);
        }
    }
    
    public void removeBook(int bookId) {
        cartItems.removeIf(b -> b.getId() == bookId);
    }
    
    public List<Book> getCartItems() {
        return cartItems;
    }
    
    public boolean containsBook(int bookId) {
        return cartItems.stream().anyMatch(b -> b.getId() == bookId);
    }
    
    public void clearCart() {
        cartItems.clear();
    }
    
    public int getCartCount() {
        return cartItems.size();
    }
}
