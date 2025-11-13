package com.example.jvm.ex;

import java.io.FileInputStream;
import java.io.IOException;

public class ReadFile {
    public void readFile(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
    }

//    public void readFile(String path) throws IOException {
//        try (FileInputStream fis = new FileInputStream(path)) {
//        }
//    }
}
