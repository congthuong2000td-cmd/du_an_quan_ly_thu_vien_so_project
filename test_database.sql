-- =============================================
-- Test Script: LibraryDB Database
-- Purpose: Verify database structure and data
-- =============================================

USE LibraryDB;
GO

-- =============================================
-- 1. COUNT RECORDS IN EACH TABLE
-- =============================================
SELECT 'Total Users' AS [Table Name], COUNT(*) AS [Count] FROM users
UNION ALL
SELECT 'Total Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Total Books', COUNT(*) FROM books
UNION ALL
SELECT 'Total Readers', COUNT(*) FROM readers
UNION ALL
SELECT 'Total Borrow Records', COUNT(*) FROM borrow_records
UNION ALL
SELECT 'Total Reservations', COUNT(*) FROM reservations
UNION ALL
SELECT 'Total Favorites', COUNT(*) FROM favorites
UNION ALL
SELECT 'Total Reviews', COUNT(*) FROM reviews
UNION ALL
SELECT 'Total Conversations', COUNT(*) FROM conversations
UNION ALL
SELECT 'Total Messages', COUNT(*) FROM messages
GO

-- =============================================
-- 2. TEST USER AUTHENTICATION
-- =============================================
PRINT '=== USERS ===';
SELECT id, username, full_name, role, active, status FROM users ORDER BY id;
GO

-- =============================================
-- 3. TEST CATEGORIES
-- =============================================
PRINT '=== CATEGORIES (First 5) ===';
SELECT TOP 5 id, name FROM categories ORDER BY id;
GO

-- =============================================
-- 4. TEST BOOKS
-- =============================================
PRINT '=== BOOKS (First 5) ===';
SELECT TOP 5 id, isbn, title, author, category_id, quantity, available FROM books ORDER BY id;
GO

-- =============================================
-- 5. TEST READERS
-- =============================================
PRINT '=== READERS ===';
SELECT id, code, name, email, phone, type FROM readers ORDER BY id;
GO

-- =============================================
-- 6. TEST BORROW RECORDS
-- =============================================
PRINT '=== BORROW RECORDS ===';
SELECT id, reader_id, book_id, borrow_date, due_date, status FROM borrow_records ORDER BY id DESC;
GO

-- =============================================
-- 7. TEST CONVERSATIONS & MESSAGES
-- =============================================
PRINT '=== CONVERSATIONS ===';
SELECT id, name, type, created_at FROM conversations ORDER BY id;
GO

PRINT '=== MESSAGES (First 10) ===';
SELECT TOP 10 id, conversation_id, sender_id, content, sent_at, is_seen FROM messages ORDER BY id DESC;
GO

-- =============================================
-- 8. TEST JOIN QUERIES
-- =============================================
PRINT '=== BOOKS WITH CATEGORIES ===';
SELECT TOP 5 
    b.title, 
    b.author, 
    c.name AS [Category], 
    b.quantity, 
    b.available 
FROM books b
LEFT JOIN categories c ON b.category_id = c.id
ORDER BY b.id;
GO

-- =============================================
-- 9. VERIFY FOREIGN KEYS
-- =============================================
PRINT '=== VERIFY FOREIGN KEY RELATIONSHIPS ===';
-- Books referencing Categories
SELECT COUNT(*) AS [Books with valid Category]
FROM books b
WHERE b.category_id IS NOT NULL 
  AND EXISTS (SELECT 1 FROM categories c WHERE c.id = b.category_id);

-- Borrow Records referencing Readers & Books
SELECT COUNT(*) AS [Borrow Records with valid Reader & Book]
FROM borrow_records br
WHERE EXISTS (SELECT 1 FROM readers r WHERE r.id = br.reader_id)
  AND EXISTS (SELECT 1 FROM books b WHERE b.id = br.book_id);

-- Messages referencing Conversations & Users
SELECT COUNT(*) AS [Messages with valid Conversation & Sender]
FROM messages m
WHERE EXISTS (SELECT 1 FROM conversations c WHERE c.id = m.conversation_id)
  AND EXISTS (SELECT 1 FROM users u WHERE u.id = m.sender_id);
GO

-- =============================================
-- 10. CHECK DATA INTEGRITY
-- =============================================
PRINT '=== DATA INTEGRITY CHECK ===';

-- Check for NULL values in critical fields
SELECT 'Users with NULL username' AS [Issue]
WHERE EXISTS (SELECT 1 FROM users WHERE username IS NULL)
UNION ALL
SELECT 'Books with NULL title'
WHERE EXISTS (SELECT 1 FROM books WHERE title IS NULL)
UNION ALL
SELECT 'Books with NULL author'
WHERE EXISTS (SELECT 1 FROM books WHERE author IS NULL)
UNION ALL
SELECT 'Readers with NULL code'
WHERE EXISTS (SELECT 1 FROM readers WHERE code IS NULL)
UNION ALL
SELECT '✓ All critical data is valid' 
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username IS NULL)
  AND NOT EXISTS (SELECT 1 FROM books WHERE title IS NULL)
  AND NOT EXISTS (SELECT 1 FROM books WHERE author IS NULL)
  AND NOT EXISTS (SELECT 1 FROM readers WHERE code IS NULL);
GO

-- =============================================
-- 11. SAMPLE DATA VERIFICATION
-- =============================================
PRINT '=== SAMPLE DATA ===';
SELECT 
    (SELECT COUNT(*) FROM users WHERE active = 1) AS [Active Users],
    (SELECT COUNT(*) FROM books WHERE quantity > 0) AS [Books in Stock],
    (SELECT COUNT(*) FROM borrow_records WHERE status = 'BORROWING') AS [Active Borrows],
    (SELECT COUNT(*) FROM readers WHERE type = N'Sinh viên') AS [Student Readers];
GO

-- =============================================
-- 12. TEST DATETIME COLUMNS (SQL Server format)
-- =============================================
PRINT '=== DATETIME COLUMNS ===';
SELECT 
    'users.last_seen' AS [Column], 
    CONVERT(VARCHAR(30), MAX(last_seen), 121) AS [Latest], 
    CONVERT(VARCHAR(30), MIN(last_seen), 121) AS [Oldest]
FROM users
WHERE last_seen IS NOT NULL
UNION ALL
SELECT 'books.created_at', CONVERT(VARCHAR(30), MAX(created_at), 121), CONVERT(VARCHAR(30), MIN(created_at), 121) FROM books
UNION ALL
SELECT 'messages.sent_at', CONVERT(VARCHAR(30), MAX(sent_at), 121), CONVERT(VARCHAR(30), MIN(sent_at), 121) FROM messages
GO

-- =============================================
-- SUCCESS MESSAGE
-- =============================================
PRINT '=== DATABASE TEST COMPLETE ===';
PRINT 'All queries executed successfully!';
GO
