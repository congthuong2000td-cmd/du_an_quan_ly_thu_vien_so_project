package com.library.util;

import com.library.model.Book;
import com.library.model.Reader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {

    // BOOKS

    public static void exportBooks(File file, List<Book> books) throws Exception {
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader("ISBN", "Tên sách", "Tác giả", "Mã thể loại", "NXB", "Năm XB", "Tổng", "Còn lại", "Mô tả", "Nội dung")
                .build();
        try (FileWriter out = new FileWriter(file);
             CSVPrinter printer = new CSVPrinter(out, format)) {
            for (Book b : books) {
                printer.printRecord(
                        b.getIsbn() == null ? "" : b.getIsbn(),
                        b.getTitle() == null ? "" : b.getTitle(),
                        b.getAuthor() == null ? "" : b.getAuthor(),
                        b.getCategoryId(),
                        b.getPublisher() == null ? "" : b.getPublisher(),
                        b.getYear(),
                        b.getQuantity(),
                        b.getAvailable(),
                        b.getDescription() == null ? "" : b.getDescription(),
                        b.getContent() == null ? "" : b.getContent()
                );
            }
        }
    }

    public static List<Book> importBooks(File file) throws Exception {
        List<Book> list = new ArrayList<>();
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader("ISBN", "Tên sách", "Tác giả", "Mã thể loại", "NXB", "Năm XB", "Tổng", "Còn lại", "Mô tả", "Nội dung")
                .setSkipHeaderRecord(true)
                .build();
        try (java.io.Reader in = new FileReader(file);
             CSVParser parser = new CSVParser(in, format)) {
            for (CSVRecord record : parser) {
                Book b = new Book();
                b.setIsbn(record.get("ISBN"));
                b.setTitle(record.get("Tên sách"));
                b.setAuthor(record.get("Tác giả"));
                try { b.setCategoryId(Integer.parseInt(record.get("Mã thể loại"))); } catch (Exception e) { b.setCategoryId(1); }
                b.setPublisher(record.get("NXB"));
                try { b.setYear(Integer.parseInt(record.get("Năm XB"))); } catch (Exception e) { b.setYear(2024); }
                try { b.setQuantity(Integer.parseInt(record.get("Tổng"))); } catch (Exception e) { b.setQuantity(1); }
                try { b.setAvailable(Integer.parseInt(record.get("Còn lại"))); } catch (Exception e) { b.setAvailable(b.getQuantity()); }
                b.setDescription(record.get("Mô tả"));
                b.setContent(record.get("Nội dung"));
                list.add(b);
            }
        }
        return list;
    }

    // READERS

    public static void exportReaders(File file, List<Reader> readers) throws Exception {
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader("Mã ĐG", "Họ tên", "Email", "SĐT", "Địa chỉ", "Loại")
                .build();
        try (FileWriter out = new FileWriter(file);
             CSVPrinter printer = new CSVPrinter(out, format)) {
            for (Reader r : readers) {
                printer.printRecord(
                        r.getCode() == null ? "" : r.getCode(),
                        r.getName() == null ? "" : r.getName(),
                        r.getEmail() == null ? "" : r.getEmail(),
                        r.getPhone() == null ? "" : r.getPhone(),
                        r.getAddress() == null ? "" : r.getAddress(),
                        r.getType() == null ? "" : r.getType()
                );
            }
        }
    }

    public static List<Reader> importReaders(File file) throws Exception {
        List<Reader> list = new ArrayList<>();
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader("Mã ĐG", "Họ tên", "Email", "SĐT", "Địa chỉ", "Loại")
                .setSkipHeaderRecord(true)
                .build();
        try (java.io.Reader in = new FileReader(file);
             CSVParser parser = new CSVParser(in, format)) {
            for (CSVRecord record : parser) {
                Reader r = new Reader();
                r.setCode(record.get("Mã ĐG"));
                r.setName(record.get("Họ tên"));
                r.setEmail(record.get("Email"));
                r.setPhone(record.get("SĐT"));
                r.setAddress(record.get("Địa chỉ"));
                r.setType(record.get("Loại"));
                list.add(r);
            }
        }
        return list;
    }

    // BORROW RECORDS

    public static void exportBorrows(List<com.library.model.BorrowRecord> records) {
        try {
            File file = new File(System.getProperty("user.home"), "borrow_records.csv");
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader("Mã Phiếu", "Độc giả", "Sách", "Ngày Mượn", "Hạn Trả", "Ngày Trả", "Trạng Thái", "Tiền Phạt")
                    .build();
            try (FileWriter out = new FileWriter(file);
                 CSVPrinter printer = new CSVPrinter(out, format)) {
                for (com.library.model.BorrowRecord r : records) {
                    printer.printRecord(
                            r.getId(),
                            r.getReaderName(),
                            r.getBookTitle(),
                            r.getBorrowDate(),
                            r.getDueDate(),
                            r.getReturnDate() == null ? "" : r.getReturnDate(),
                            r.getStatusDisplay(),
                            r.getFine()
                    );
                }
            }
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Đã xuất dữ liệu ra file: " + file.getAbsolutePath()).show();
        } catch (Exception e) {
            e.printStackTrace();
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Lỗi khi xuất file Excel!").show();
        }
    }
}
