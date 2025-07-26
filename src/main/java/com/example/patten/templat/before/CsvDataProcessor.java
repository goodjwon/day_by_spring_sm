package com.example.patten.templat.before;

public class CsvDataProcessor {
    public void process() {
        System.out.println("---CSV 파일 생성 시작---");
        //1. 데이터 조회 (중복)
        System.out.println("[공통] 데이터베이스에서 조회합니다.");
        String data = "id,name,role";

        //2. 데이터 가공 (고유 로직)
        System.out.println("[CSV] 데이터를 쉽표(,)로 구분된 형식으로 변환합니다.");
        String processedData = data.replace(",",", ");

        //3. 파일 저장 (중복)
        System.out.println("[공통] 변환된 데이터를 파일에 씁니다: " + processedData);
        String fileName = "data.csv";
        System.out.println("[저장] " + fileName + "파일이 성공적으로 생성되었습니다.");
        System.out.println("----------------\n");
    }
}
