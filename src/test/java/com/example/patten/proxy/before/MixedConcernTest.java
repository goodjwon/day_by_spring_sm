package com.example.patten.proxy.before;

import org.junit.jupiter.api.Test;

public class MixedConcernTest {
    @Test
    void testMixedService() {
        EventService eventService = new EventService();
        eventService.processEvent("신규 회원 가입");
    }
}
