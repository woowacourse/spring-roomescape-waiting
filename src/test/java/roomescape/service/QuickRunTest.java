package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Reservation;

@SpringBootTest
class QuickRunTest {
    @Autowired
    SessionService sessionService;

    @Test
    void run() {
        Reservation r = sessionService.findReservationById(1L);
        System.out.println(r.getSession().getTimeSlot().getStartAt());
    }
}