package com.example.jvm.ex;

import org.apache.catalina.User;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private static Map<String, User> sessions = new HashMap<>();

    public static void login(String id, User user) {
        sessions.put(id, user);
    }

    public static void logout(String id) {
        sessions.remove(id);
    }
}
