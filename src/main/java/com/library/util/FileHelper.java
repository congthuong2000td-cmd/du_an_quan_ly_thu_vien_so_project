package com.library.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileHelper {
    private static final String UPLOAD_DIR = "uploads/chat/";

    public static String saveChatFile(File file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String extension = "";
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) extension = fileName.substring(i);

            String newFileName = UUID.randomUUID().toString() + extension;
            File dest = new File(UPLOAD_DIR + newFileName);
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return dest.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
