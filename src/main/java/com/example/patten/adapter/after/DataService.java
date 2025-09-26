package com.example.patten.adapter.after;

import com.example.patten.adapter.before.DataProcessor;

// 개선된 DataService
public class DataService {
    public void process(DataProcessor processor) {
        // 클라이언트는 이제 표준 인터페이스만 호출하면 끝!
        processor.processData();
    }
}