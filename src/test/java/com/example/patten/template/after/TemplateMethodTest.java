package com.example.patten.template.after;

import com.example.patten.templat.after.AbstractDataProcessor;
import com.example.patten.templat.after.CsvDataProcessor;
import com.example.patten.templat.after.TxtDataProcessor;
import org.junit.jupiter.api.Test;

public class TemplateMethodTest {
    @Test
    void templateMethodPatternTest() {
        System.out.println("✨ 템플릿 메서드 패턴 적용 후 코드 ✨");
        System.out.println("----------------------------------");

        AbstractDataProcessor csvProcessor = new CsvDataProcessor();
        csvProcessor.process(); // 템플릿 메서드 호출

        AbstractDataProcessor txtProcessor = new TxtDataProcessor();
        txtProcessor.process(); // 템플릿 메서드 호출
    }
}
