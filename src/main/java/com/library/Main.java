package com.library;

public class Main {
    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.marlin.useUnsafe", "false");
        App.main(args);
    }
}
