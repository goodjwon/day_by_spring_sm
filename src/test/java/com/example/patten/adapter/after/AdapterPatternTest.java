package com.example.patten.adapter.after;

// AdapterPatternTest.java
import com.example.patten.adapter.before.DataProcessor;
import com.example.patten.adapter.before.NewJsonLibrary;
import com.example.patten.adapter.before.XmlDataProcessor;
import org.junit.jupiter.api.Test;

public class AdapterPatternTest {
    @Test
    void adapterTest() {
        System.out.println("✨ 어댑터 패턴 적용 후 테스트 ✨");
        DataService dataService = new DataService();

        // 1. 기존 방식은 그대로 사용 가능
        System.out.println("\n--- XML 처리 (기존 방식) ---");
        DataProcessor xmlProcessor = new XmlDataProcessor();
        dataService.process(xmlProcessor);

        // 2. 새로운 JSON 라이브러리는 '어댑터'로 감싸서 전달
        System.out.println("\n--- JSON 처리 (어댑터 사용) ---");
        NewJsonLibrary newJsonLibrary = new NewJsonLibrary();
        DataProcessor jsonAdapter = new JsonAdapter(newJsonLibrary, "{'id':'user1'}");
        dataService.process(jsonAdapter);
    }
}