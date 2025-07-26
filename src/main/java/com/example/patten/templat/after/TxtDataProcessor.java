package com.example.patten.templat.after;

public class TxtDataProcessor extends AbstractDataProcessor{
    @Override
    protected String transformData(String data) {
        System.out.println("[TXT] 데이터를 공백( )으로 구분된 형식으로 변환합니다.");
        return data.replace(",", " ");
    }

    @Override
    protected String getFileName() {
        return "data.txt";
    }
}
