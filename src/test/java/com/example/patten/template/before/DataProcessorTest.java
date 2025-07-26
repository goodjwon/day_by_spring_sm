package com.example.patten.template.before;

import com.example.patten.templat.before.CsvDataProcessor;
import com.example.patten.templat.before.TxtDataProcessor;
import org.junit.jupiter.api.Test;

public class DataProcessorTest {
    @Test
    void testProcessing() {
        CsvDataProcessor csvProcessor = new CsvDataProcessor();
        csvProcessor.process();

        TxtDataProcessor txtProcessor = new TxtDataProcessor();
        txtProcessor.process();
    }
}
