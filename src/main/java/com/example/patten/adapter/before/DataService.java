package com.example.patten.adapter.before;

// 클라이언트 코드: DataService
public class DataService {
    public void process(Object processor, String data) {
        // 클라이언트가 직접 타입을 확인하고 분기 처리 (나쁜 설계)
        if (processor instanceof XmlDataProcessor) {
            ((XmlDataProcessor) processor).processData();
        } else if (processor instanceof NewJsonLibrary) {
            // 호환되지 않으므로 직접 호출
            ((NewJsonLibrary) processor).parseAndRunJson(data);
        }
    }
}
