package roomescape.reservation.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.Reservation;

@SpringBootTest
public class JpaReservationRepositoryTest {
    @Autowired
    private JpaReservationRepository reservationRepository;

    @Test
    @Transactional
    void sql_확인용() {

        Reservation reservation = reservationRepository.findById(1L)
                .orElseThrow();

        reservation.getTime().getStartAt();

    }
}
