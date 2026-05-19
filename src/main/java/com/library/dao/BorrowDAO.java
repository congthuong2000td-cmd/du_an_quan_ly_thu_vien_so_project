package com.library.dao;

import com.library.model.BorrowRecord;
import com.library.util.Constants;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BorrowDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<BorrowRecord> getAll() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, r.name as reader_name, b.title as book_title
            FROM borrow_records br
            JOIN readers r ON br.reader_id = r.id
            JOIN books b ON br.book_id = b.id
            ORDER BY br.id DESC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) records.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    public List<BorrowRecord> getActiveRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, r.name as reader_name, b.title as book_title
            FROM borrow_records br
            JOIN readers r ON br.reader_id = r.id
            JOIN books b ON br.book_id = b.id
            WHERE br.status IN ('BORROWING', 'OVERDUE')
            ORDER BY br.due_date ASC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) records.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    public List<BorrowRecord> getRecentRecords(int limit) {
        List<BorrowRecord> records = new ArrayList<>();
        String limitClause = com.library.util.Constants.DB_URL.startsWith("jdbc:sqlite") 
            ? " LIMIT ?" : " OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        String sql = "SELECT br.*, r.name as reader_name, b.title as book_title\n" +
            "FROM borrow_records br\n" +
            "JOIN readers r ON br.reader_id = r.id\n" +
            "JOIN books b ON br.book_id = b.id\n" +
            "ORDER BY br.id DESC" + limitClause;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    public boolean renewBook(int borrowId) {
        String sql = "UPDATE borrow_records SET due_date = date(due_date, '+7 days') WHERE id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<BorrowRecord> getByReaderName(String readerName) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, r.name as reader_name, b.title as book_title
            FROM borrow_records br
            JOIN readers r ON br.reader_id = r.id
            JOIN books b ON br.book_id = b.id
            WHERE LOWER(TRIM(r.name)) = LOWER(TRIM(?))
            ORDER BY br.id DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, readerName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    public boolean borrowBook(int readerId, int bookId, LocalDate borrowDate, LocalDate dueDate) {
        String sql = "INSERT INTO borrow_records (reader_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, readerId);
            ps.setInt(2, bookId);
            ps.setString(3, borrowDate.toString());
            ps.setString(4, dueDate.toString());
            ps.setString(5, Constants.STATUS_BORROWING);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean returnBook(int recordId, double fine) {
        String sql = "UPDATE borrow_records SET return_date=?, fine=?, status=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            ps.setDouble(2, fine);
            ps.setString(3, Constants.STATUS_RETURNED);
            ps.setInt(4, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void updateOverdueStatus() {
        String sql = "UPDATE borrow_records SET status=? WHERE status=? AND due_date < ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, Constants.STATUS_OVERDUE);
            ps.setString(2, Constants.STATUS_BORROWING);
            ps.setString(3, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean markAsLost(int recordId, double fine) {
        String sql = "UPDATE borrow_records SET return_date=?, fine=?, status='LOST' WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            ps.setDouble(2, fine);
            ps.setInt(3, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getActiveBorrowCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM borrow_records WHERE status IN ('BORROWING','OVERDUE')")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getOverdueCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM borrow_records WHERE status='OVERDUE' OR (status='BORROWING' AND due_date < date('now'))")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Integer> getBorrowsByMonth() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
            SELECT strftime('%m/%Y', borrow_date) as month, COUNT(*) as count
            FROM borrow_records
            WHERE borrow_date >= date('now', '-6 months')
            GROUP BY strftime('%Y-%m', borrow_date)
            ORDER BY borrow_date ASC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("month"), rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    public Map<String, Integer> getTopBorrowedBooks(int limit) {
        Map<String, Integer> data = new LinkedHashMap<>();
        String limitClause = com.library.util.Constants.DB_URL.startsWith("jdbc:sqlite") 
            ? " LIMIT ?" : " OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        String sql = "SELECT b.title, COUNT(*) as count\n" +
            "FROM borrow_records br JOIN books b ON br.book_id = b.id\n" +
            "GROUP BY br.book_id, b.title ORDER BY count DESC" + limitClause;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("title"), rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    public Map<String, Integer> getBooksByCategory() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
            SELECT c.name, COUNT(b.id) as count
            FROM categories c LEFT JOIN books b ON c.id = b.category_id
            GROUP BY c.id HAVING count > 0 ORDER BY count DESC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("name"), rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    private BorrowRecord mapResultSet(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setReaderId(rs.getInt("reader_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setReaderName(rs.getString("reader_name"));
        record.setBookTitle(rs.getString("book_title"));
        String borrowDate = rs.getString("borrow_date");
        if (borrowDate != null) record.setBorrowDate(LocalDate.parse(borrowDate));
        String dueDate = rs.getString("due_date");
        if (dueDate != null) record.setDueDate(LocalDate.parse(dueDate));
        String returnDate = rs.getString("return_date");
        if (returnDate != null) record.setReturnDate(LocalDate.parse(returnDate));
        record.setFine(rs.getDouble("fine"));
        record.setStatus(rs.getString("status"));
        return record;
    }
}
