-- =============================================
-- SQL Server Schema: Library Management System
-- =============================================

CREATE DATABASE LibraryDB;
GO
USE LibraryDB;
GO

-- 1. Bảng categories
CREATE TABLE categories (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL UNIQUE
);

-- 2. Bảng books
CREATE TABLE books (
    id INT PRIMARY KEY IDENTITY(1,1),
    isbn VARCHAR(50) UNIQUE,
    title NVARCHAR(255) NOT NULL,
    author NVARCHAR(255) NOT NULL,
    publisher NVARCHAR(255),
    year INT,
    category_id INT,
    quantity INT DEFAULT 1,
    available INT DEFAULT 1,
    description NVARCHAR(MAX),
    content NVARCHAR(MAX),
    cover_image VARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 3. Bảng readers
CREATE TABLE readers (
    id INT PRIMARY KEY IDENTITY(1,1),
    code VARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address NVARCHAR(500),
    type NVARCHAR(50) DEFAULT N'Sinh viên',
    created_at DATETIME DEFAULT GETDATE()
);

-- 4. Bảng borrow_records
CREATE TABLE borrow_records (
    id INT PRIMARY KEY IDENTITY(1,1),
    reader_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    fine FLOAT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'BORROWING',
    FOREIGN KEY (reader_id) REFERENCES readers(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 5. Bảng users
CREATE TABLE users (
    id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'LIBRARIAN',
    active INT DEFAULT 0,
    security_question NVARCHAR(255),
    security_answer NVARCHAR(255),
    status VARCHAR(50) DEFAULT 'OFFLINE',
    last_seen DATETIME
);

-- 6. Bảng reservations
CREATE TABLE reservations (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    request_date DATE DEFAULT CAST(GETDATE() AS DATE),
    response_date DATE,
    note NVARCHAR(MAX),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 7. Bảng favorites
CREATE TABLE favorites (
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 8. Bảng reviews
CREATE TABLE reviews (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    rating INT DEFAULT 0,
    comment NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 9. Bảng conversations
CREATE TABLE conversations (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255),
    type VARCHAR(50) DEFAULT 'DIRECT',
    created_at DATETIME DEFAULT GETDATE()
);

-- 10. Bảng conversation_members
CREATE TABLE conversation_members (
    conversation_id INT,
    user_id INT,
    joined_at DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 11. Bảng messages
CREATE TABLE messages (
    id INT PRIMARY KEY IDENTITY(1,1),
    conversation_id INT,
    sender_id INT,
    content NVARCHAR(MAX),
    type VARCHAR(50) DEFAULT 'TEXT',
    file_path NVARCHAR(500),
    sent_at DATETIME DEFAULT GETDATE(),
    is_seen INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 12. Bảng chat_notifications
CREATE TABLE chat_notifications (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT,
    message_id INT,
    is_read INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (message_id) REFERENCES messages(id)
);
GO

-- =============================================
-- Seed Data: Danh mục sách
-- =============================================
INSERT INTO categories (name) VALUES
(N'Văn học'), (N'Khoa học'), (N'Công nghệ'), (N'Lịch sử'),
(N'Kinh tế'), (N'Giáo dục'), (N'Truyện'), (N'Tâm lý học'),
(N'Triết học'), (N'Ngoại ngữ');
GO

-- =============================================
-- Seed Data: Tài khoản admin và thủ thư
-- (Mật khẩu admin = "admin", thủ thư = "123456" - đã hash SHA-256)
-- =============================================
INSERT INTO users (username, password, full_name, role, active, security_question, security_answer)
VALUES ('admin',
        'jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=',
        N'Quản trị viên', 'ADMIN', 1,
        N'Tên trường học đầu tiên của bạn?', 'Library');

INSERT INTO users (username, password, full_name, role, active, security_question, security_answer)
VALUES ('thuthu',
        'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIFApI63J4=',
        N'Nguyễn Văn Thủ Thư', 'LIBRARIAN', 1,
        N'Tên trường học đầu tiên của bạn?', 'Library');
GO

-- =============================================
-- Seed Data: Độc giả mẫu
-- =============================================
INSERT INTO readers (code, name, email, phone, address, type) VALUES
('DG001', N'Nguyễn Văn An',   'an.nguyen@email.com',   '0901234567', N'Hà Nội',       N'Sinh viên'),
('DG002', N'Trần Thị Bình',   'binh.tran@email.com',   '0912345678', N'Hồ Chí Minh',  N'Sinh viên'),
('DG003', N'Lê Văn Cường',    'cuong.le@email.com',    '0923456789', N'Đà Nẵng',      N'Giảng viên'),
('DG004', N'Phạm Thị Duyên',  'duyen.pham@email.com',  '0934567890', N'Hải Phòng',    N'Sinh viên'),
('DG005', N'Hoàng Văn Em',    'em.hoang@email.com',    '0945678901', N'Cần Thơ',      N'Khác');
GO

-- =============================================
-- Seed Data: Sách mẫu
-- =============================================
INSERT INTO books (isbn, title, author, publisher, year, category_id, quantity, available, description) VALUES
('978-604-1-00001', N'Truyện Kiều',           N'Nguyễn Du',          N'NXB Văn Học',         1820, 1, 5,  5,  N'Tác phẩm văn học kinh điển Việt Nam'),
('978-604-1-00002', N'Dế Mèn Phiêu Lưu Ký',  N'Tô Hoài',            N'NXB Kim Đồng',        1941, 7, 3,  3,  N'Truyện thiếu nhi nổi tiếng'),
('978-604-1-00003', N'Lập Trình Java',         N'Nguyễn Văn A',       N'NXB Giáo Dục',        2023, 3, 10, 10, N'Giáo trình lập trình Java cơ bản đến nâng cao'),
('978-604-1-00004', N'Lịch Sử Việt Nam',      N'Trần Trọng Kim',     N'NXB Khoa Học',        1920, 4, 4,  4,  N'Việt Nam sử lược'),
('978-604-1-00005', N'Kinh Tế Học Vĩ Mô',    N'Nguyễn Văn B',       N'NXB Kinh Tế',         2022, 5, 6,  6,  N'Giáo trình kinh tế học vĩ mô'),
('978-604-1-00006', N'Tâm Lý Học Đại Cương', N'Nguyễn Quang Uẩn',  N'NXB Đại Học Sư Phạm', 2021, 8, 7,  7,  N'Giáo trình tâm lý học'),
('978-604-1-00007', N'Vật Lý Đại Cương',     N'Lương Duyên Bình',   N'NXB Giáo Dục',        2020, 2, 8,  8,  N'Giáo trình vật lý'),
('978-604-1-00008', N'Tiếng Anh Giao Tiếp',  N'Nguyễn Thị C',       N'NXB Ngoại Ngữ',       2023, 10, 5, 5,  N'Sách học tiếng Anh giao tiếp'),
('978-604-1-00009', N'Đắc Nhân Tâm',         N'Dale Carnegie',       N'NXB Tổng Hợp',        1936, 8, 12, 12, N'How to Win Friends and Influence People'),
('978-604-1-00010', N'Nhà Giả Kim',           N'Paulo Coelho',        N'NXB Hội Nhà Văn',     1988, 1, 9,  9,  N'The Alchemist - Tiểu thuyết nổi tiếng thế giới');
GO
