package com.example.patten.templat.after;

public class CsvDataProcessor extends AbstractDataProcessor{
    @Override
    protected String transformData(String data) {
        System.out.println("[CSV] 데이터를 쉼표(,)로 구분된 형식으로 변환합니다.");
        return data.replace(",", ", ");
    }

    @Override
    protected String getFileName() {
        return "data.csv";
    }
}
