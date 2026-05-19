package com.library.dao;

import com.library.util.Constants;
import com.library.util.ValidationUtils;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(Constants.DB_URL);
            if (Constants.DB_URL.startsWith("jdbc:sqlite")) {
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public void initializeDatabase() {
        if (!Constants.DB_URL.startsWith("jdbc:sqlite")) {
            System.out.println("Using SQL Server. Schema should be initialized via SSMS script.");
            return;
        }
        
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();

            // Create categories table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            // Create books table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    isbn TEXT UNIQUE,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    publisher TEXT,
                    year INTEGER,
                    category_id INTEGER,
                    quantity INTEGER DEFAULT 1,
                    available INTEGER DEFAULT 1,
                    description TEXT,
                    cover_image TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                )
            """);

            // Create readers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS readers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    address TEXT,
                    type TEXT DEFAULT 'Sinh viên',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create borrow_records table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS borrow_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reader_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    borrow_date DATE NOT NULL,
                    due_date DATE NOT NULL,
                    return_date DATE,
                    fine REAL DEFAULT 0,
                    status TEXT DEFAULT 'BORROWING',
                    FOREIGN KEY (reader_id) REFERENCES readers(id),
                    FOREIGN KEY (book_id) REFERENCES books(id)
                )
            """);

            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT DEFAULT 'LIBRARIAN',
                    active INTEGER DEFAULT 0,
                    security_question TEXT,
                    security_answer TEXT
                )
            """);

            // Migration: add 'active' column if not exists
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN active INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            // Always ensure existing accounts are active if not specified
            stmt.execute("UPDATE users SET active = 1 WHERE active = 0 AND (username = 'admin' OR username = 'thuthu')");
            stmt.execute("UPDATE users SET active = 1 WHERE active IS NULL");

            // Migration: add security columns if not exists
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN security_question TEXT");
                stmt.execute("ALTER TABLE users ADD COLUMN security_answer TEXT");
                // Set default security question for existing accounts
                stmt.execute("UPDATE users SET security_question = 'Tên trường học đầu tiên của bạn?', security_answer = 'Library' " +
                        "WHERE security_question IS NULL");
            } catch (SQLException ignored) {
                // Columns already exist
            }

            // Create reservations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    request_date DATE DEFAULT CURRENT_DATE,
                    response_date DATE,
                    note TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (book_id) REFERENCES books(id)
                )
            """);

            // Migration: add 'content' column to books if not exists
            try {
                stmt.execute("ALTER TABLE books ADD COLUMN content TEXT");
            } catch (SQLException ignored) {
                // Column already exists
            }

            // Create favorites table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    user_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, book_id),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (book_id) REFERENCES books(id)
                )
            """);

            // Create reviews table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reviews (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    rating INTEGER DEFAULT 0,
                    comment TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (book_id) REFERENCES books(id)
                )
            """);

            // Create conversations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS conversations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    type TEXT DEFAULT 'DIRECT',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create conversation_members table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS conversation_members (
                    conversation_id INTEGER,
                    user_id INTEGER,
                    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (conversation_id, user_id),
                    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create messages table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    conversation_id INTEGER,
                    sender_id INTEGER,
                    content TEXT,
                    type TEXT DEFAULT 'TEXT',
                    file_path TEXT,
                    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    is_seen INTEGER DEFAULT 0,
                    is_deleted INTEGER DEFAULT 0,
                    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
                    FOREIGN KEY (sender_id) REFERENCES users(id)
                )
            """);

            // Create chat_notifications table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chat_notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    message_id INTEGER,
                    is_read INTEGER DEFAULT 0,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (message_id) REFERENCES messages(id)
                )
            """);

            // Migration: add 'status' and 'last_seen' to users
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN status TEXT DEFAULT 'OFFLINE'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN last_seen DATETIME");
            } catch (SQLException ignored) {}

            seedData(conn);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedData(Connection conn) throws SQLException {
        // Seed admin user if not exists
        PreparedStatement checkUser = conn.prepareStatement("SELECT COUNT(*) FROM users");
        ResultSet rs = checkUser.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password, full_name, role, active, security_question, security_answer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            // Admin account (active = 1)
            ps.setString(1, "admin");
            ps.setString(2, ValidationUtils.hashPassword("admin"));
            ps.setString(3, "Quản trị viên");
            ps.setString(4, Constants.ROLE_ADMIN);
            ps.setInt(5, 1);
            ps.setString(6, "Tên trường học đầu tiên của bạn?");
            ps.setString(7, "Library");
            ps.executeUpdate();

            // Librarian account (active = 1)
            ps.setString(1, "thuthu");
            ps.setString(2, ValidationUtils.hashPassword("123456"));
            ps.setString(3, "Nguyễn Văn Thủ Thư");
            ps.setString(4, Constants.ROLE_LIBRARIAN);
            ps.setInt(5, 1);
            ps.setString(6, "Tên trường học đầu tiên của bạn?");
            ps.setString(7, "Library");
            ps.executeUpdate();
        }

        // Seed categories
        PreparedStatement checkCat = conn.prepareStatement("SELECT COUNT(*) FROM categories");
        rs = checkCat.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
            String[] categories = {
                "Văn học", "Khoa học", "Công nghệ", "Lịch sử",
                "Kinh tế", "Giáo dục", "Truyện", "Tâm lý học",
                "Triết học", "Ngoại ngữ"
            };
            PreparedStatement ps = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)");
            for (String cat : categories) {
                ps.setString(1, cat);
                ps.executeUpdate();
            }
        }

        // Seed sample books
        PreparedStatement checkBooks = conn.prepareStatement("SELECT COUNT(*) FROM books");
        rs = checkBooks.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
            String[][] books = {
                {"978-604-1-00001", "Truyện Kiều", "Nguyễn Du", "NXB Văn Học", "1820", "1", "5", "Tác phẩm văn học kinh điển Việt Nam"},
                {"978-604-1-00002", "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "NXB Kim Đồng", "1941", "7", "3", "Truyện thiếu nhi nổi tiếng"},
                {"978-604-1-00003", "Lập Trình Java", "Nguyễn Văn A", "NXB Giáo Dục", "2023", "3", "10", "Giáo trình lập trình Java cơ bản đến nâng cao"},
                {"978-604-1-00004", "Lịch Sử Việt Nam", "Trần Trọng Kim", "NXB Khoa Học", "1920", "4", "4", "Việt Nam sử lược"},
                {"978-604-1-00005", "Kinh Tế Học Vĩ Mô", "Nguyễn Văn B", "NXB Kinh Tế", "2022", "5", "6", "Giáo trình kinh tế học vĩ mô"},
                {"978-604-1-00006", "Tâm Lý Học Đại Cương", "Nguyễn Quang Uẩn", "NXB Đại Học Sư Phạm", "2021", "8", "7", "Giáo trình tâm lý học"},
                {"978-604-1-00007", "Vật Lý Đại Cương", "Lương Duyên Bình", "NXB Giáo Dục", "2020", "2", "8", "Giáo trình vật lý"},
                {"978-604-1-00008", "Tiếng Anh Giao Tiếp", "Nguyễn Thị C", "NXB Ngoại Ngữ", "2023", "10", "5", "Sách học tiếng Anh giao tiếp"},
                {"978-604-1-00009", "Đắc Nhân Tâm", "Dale Carnegie", "NXB Tổng Hợp", "1936", "8", "12", "How to Win Friends and Influence People"},
                {"978-604-1-00010", "Nhà Giả Kim", "Paulo Coelho", "NXB Hội Nhà Văn", "1988", "1", "9", "The Alchemist - Tiểu thuyết nổi tiếng thế giới"},
            };

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books (isbn, title, author, publisher, year, category_id, quantity, available, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            for (String[] book : books) {
                ps.setString(1, book[0]);
                ps.setString(2, book[1]);
                ps.setString(3, book[2]);
                ps.setString(4, book[3]);
                ps.setInt(5, Integer.parseInt(book[4]));
                ps.setInt(6, Integer.parseInt(book[5]));
                ps.setInt(7, Integer.parseInt(book[6]));
                ps.setInt(8, Integer.parseInt(book[6])); // available = quantity
                ps.setString(9, book[7]);
                ps.executeUpdate();
            }

            // Seed online content for some books
            PreparedStatement updateContent = conn.prepareStatement(
                "UPDATE books SET content=? WHERE isbn=?"
            );

            updateContent.setString(1, """
                TRUYỆN KIỀU - Nguyễn Du

                Trăm năm trong cõi người ta,
                Chữ tài chữ mệnh khéo là ghét nhau.
                Trải qua một cuộc bể dâu,
                Những điều trông thấy mà đau đớn lòng.

                Lạ gì bỉ sắc tư phong,
                Trời xanh quen thói má hồng đánh ghen.
                Cảo thơm lần giở trước đèn,
                Phong tình cổ lục còn truyền sử xanh.

                Rằng: Năm Gia Tĩnh triều Minh,
                Bốn phương phẳng lặng, hai kinh vững vàng.
                Có nhà viên ngoại họ Vương,
                Gia tư nghĩ cũng thường thường bậc trung.

                Một trai con thứ rốt lòng,
                Vương Quan là chữ, nối dòng nho gia.
                Đầu lòng hai ả tố nga,
                Thúy Kiều là chị em là Thúy Vân.

                Mai cốt cách tuyết tinh thần,
                Mỗi người một vẻ mười phân vẹn mười.
                Vân xem trang trọng khác vời,
                Khuôn trăng đầy đặn nét ngài nở nang.

                Hoa cười ngọc thốt đoan trang,
                Mây thua nước tóc tuyết nhường màu da.
                Kiều càng sắc sảo mặn mà,
                So bề tài sắc lại là phần hơn.

                [... Tiếp tục đọc tại thư viện ...]
                """);
            updateContent.setString(2, "978-604-1-00001");
            updateContent.executeUpdate();

            updateContent.setString(1, """
                DẾ MÈN PHIÊU LƯU KÝ - Tô Hoài

                Chương 1: Tôi là Dế Mèn

                Tôi sống độc lập từ thuở bé. Ấy là tôi nói theo cái ý nghĩ
                bây giờ khi nhìn lại quãng đời thơ ấu, chứ thuở ấy, tôi đâu
                biết thế nào là độc lập.

                Tôi sinh ra dưới một bụi cỏ ven bờ ruộng. Mẹ tôi đẻ tôi ra,
                bỏ tôi nằm giữa đám cỏ rậm rồi bay đi mất. Từ đấy tôi tự
                lực cánh sinh.

                Thuở nhỏ tôi rất lười, ham ăn và nghịch ngợm. Tuy thế,
                nhờ trời cho tôi có sức khỏe tốt, đôi càng mập mạp,
                đôi cánh dài bay xa, giọng hát vang vang nên tôi rất
                tự tin về mình.

                Bà con hàng xóm ai cũng bảo tôi đẹp. Mỗi khi tôi đi qua,
                các chị Cào Cào thường trầm trồ khen ngợi.

                [... Đọc tiếp chương 2 tại thư viện ...]
                """);
            updateContent.setString(2, "978-604-1-00002");
            updateContent.executeUpdate();

            updateContent.setString(1, """
                NHÀ GIẢ KIM - Paulo Coelho

                Phần Một

                Cậu bé chăn cừu tên Santiago thức dậy vừa lúc mặt trời
                bắt đầu nhô lên khỏi đường chân trời.

                "Tối qua ta lại mơ thấy giấc mơ ấy," cậu bé nói với đàn
                cừu của mình.

                Đàn cừu không nói gì, bởi vì chúng đã quen với việc
                im lặng lắng nghe cậu. Cậu bé chợt nhớ ra rằng cần
                phải tìm gặp bà già ở Tarifa để nhờ bà giải mộng.

                Cậu đã có giấc mơ ấy lần thứ hai. Trong mơ, cậu thấy
                mình đi đến kim tự tháp ở Ai Cập. "Ta phải tìm kho
                báu," cậu tự nhủ với mình.

                Cậu nhớ lại lời vị vua già từng nói: "Khi con muốn điều gì,
                cả vũ trụ sẽ hợp lực giúp con đạt được nó."

                Và đó là khởi đầu của một cuộc hành trình vĩ đại...

                [... Đọc tiếp tại thư viện ...]
                """);
            updateContent.setString(2, "978-604-1-00010");
            updateContent.executeUpdate();
        }

        // Seed sample readers
        PreparedStatement checkReaders = conn.prepareStatement("SELECT COUNT(*) FROM readers");
        rs = checkReaders.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
            String[][] readers = {
                {"DG001", "Nguyễn Văn An", "an.nguyen@email.com", "0901234567", "Hà Nội", "Sinh viên"},
                {"DG002", "Trần Thị Bình", "binh.tran@email.com", "0912345678", "Hồ Chí Minh", "Sinh viên"},
                {"DG003", "Lê Văn Cường", "cuong.le@email.com", "0923456789", "Đà Nẵng", "Giảng viên"},
                {"DG004", "Phạm Thị Duyên", "duyen.pham@email.com", "0934567890", "Hải Phòng", "Sinh viên"},
                {"DG005", "Hoàng Văn Em", "em.hoang@email.com", "0945678901", "Cần Thơ", "Khác"},
            };

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO readers (code, name, email, phone, address, type) VALUES (?, ?, ?, ?, ?, ?)"
            );
            for (String[] reader : readers) {
                ps.setString(1, reader[0]);
                ps.setString(2, reader[1]);
                ps.setString(3, reader[2]);
                ps.setString(4, reader[3]);
                ps.setString(5, reader[4]);
                ps.setString(6, reader[5]);
                ps.executeUpdate();
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
