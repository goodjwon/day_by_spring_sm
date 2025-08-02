package com.example.patten.factory.after;

public class Client {
    public void send(NotifierFactory factory, String message) {
        Notifier notifier = factory.createNotifier();
        notifier.send(message);
        System.out.println("------------------------------");
    }
}
