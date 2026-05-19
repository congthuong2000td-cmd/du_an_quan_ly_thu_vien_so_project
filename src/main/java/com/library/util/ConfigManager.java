package com.library.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties props = new Properties();

    static {
        loadConfig();
    }

    public static void loadConfig() {
        try {
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                props.load(new FileInputStream(f));
                if (props.containsKey("maxBorrowDays")) {
                    Constants.MAX_BORROW_DAYS = Integer.parseInt(props.getProperty("maxBorrowDays"));
                }
                if (props.containsKey("finePerDay")) {
                    Constants.FINE_PER_DAY = Double.parseDouble(props.getProperty("finePerDay"));
                }
                if (props.containsKey("dbUrl")) {
                    Constants.DB_URL = props.getProperty("dbUrl");
                }
            } else {
                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            props.setProperty("maxBorrowDays", String.valueOf(Constants.MAX_BORROW_DAYS));
            props.setProperty("finePerDay", String.valueOf(Constants.FINE_PER_DAY));
            props.setProperty("dbUrl", Constants.DB_URL);
            props.store(new FileOutputStream(CONFIG_FILE), "Library System Configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
