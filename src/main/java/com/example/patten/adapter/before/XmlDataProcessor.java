package com.example.patten.adapter.before;

// 기존에 있던 XML 처리기
public class XmlDataProcessor implements DataProcessor {
    @Override
    public void processData() {
        System.out.println("기존 방식으로 xml 데이터를 처리합니다.");
    }
}
