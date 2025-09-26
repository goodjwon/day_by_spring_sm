package com.example.patten.adapter.before;

import org.junit.jupiter.api.Test;

public class IncompatibleSystemTest {
    @Test
    void testProcessing() {
        DataService dataService = new DataService();
        System.out.println("--- XML 처리 ---");
        dataService.process(new XmlDataProcessor(), null);
        System.out.println("\n--- JSON 처리 ---");
        dataService.process(new NewJsonLibrary(), "{'id':'user1'}");
    }
}
