package com.library.util;

public class Constants {
    // Database
    public static final String DB_URL = "jdbc:sqlite:library.db";

    // Default config
    public static int MAX_BORROW_DAYS = 14;
    public static double FINE_PER_DAY = 5000; // 5,000 VND/ngày

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_LIBRARIAN = "LIBRARIAN";
    public static final String ROLE_READER = "READER";

    // Borrow status
    public static final String STATUS_BORROWING = "BORROWING";
    public static final String STATUS_RETURNED = "RETURNED";
    public static final String STATUS_OVERDUE = "OVERDUE";
    public static final String STATUS_LOST = "LOST";

    // Reservation status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Reader types
    public static final String TYPE_STUDENT = "Sinh viên";
    public static final String TYPE_LECTURER = "Giảng viên";
    public static final String TYPE_OTHER = "Khác";

    // App info
    public static final String APP_TITLE = "Quản Lý Thư Viện Số";
    public static final String APP_VERSION = "1.0.0";

    // Theme paths
    public static final String DARK_THEME = "/css/dark-theme.css";
    public static final String LIGHT_THEME = "/css/light-theme.css";

    // Colors
    public static final String COLOR_BLUE = "#89b4fa";
    public static final String COLOR_GREEN = "#a6e3a1";
    public static final String COLOR_RED = "#f38ba8";
    public static final String COLOR_YELLOW = "#f9e2af";
    public static final String COLOR_PEACH = "#fab387";
    public static final String COLOR_MAUVE = "#cba6f7";
    public static final String COLOR_TEAL = "#94e2d5";
}
