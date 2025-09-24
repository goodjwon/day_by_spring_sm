package com.example.patten.adapter.after;

import com.example.patten.adapter.before.DataProcessor;
import com.example.patten.adapter.before.NewJsonLibrary;

public class JsonAdapter implements DataProcessor {
    // 1. 호환되지 않는 새로운 라이브러리 객체를 내부에 가짐
    private final NewJsonLibrary newJsonLibrary;
    public final String jsonData;

    public JsonAdapter(NewJsonLibrary newJsonLibrary, String jsonData) {
        this.newJsonLibrary = newJsonLibrary;
        this.jsonData = jsonData;
    }

    // 2. 우리 시스템의 표준 메서드(processData)를 오버라이드
    @Override
    public void processData() {
        System.out.println("🔌 [어댑터 작동] 표준 processData() 호출을 -> newJsonLibrary.parseAndRunJson()으로 변환합니다.");
        // 3. 내부적으로는 실제 라이브러리의 메서드를 호출
        newJsonLibrary.parseAndRunJson(jsonData);
    }
}
