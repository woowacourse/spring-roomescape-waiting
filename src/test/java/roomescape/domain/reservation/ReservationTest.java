package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.repository.ReservationRepository;

@Sql("/waiting-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    void 예약의_상태를_예약됨으로_변경() {
        //given
        Reservation reservation = reservationRepository.findById(2L).orElseThrow();

        //when
        reservation.changeStatusToReserve();

        //then
        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
    }

    @Test
    void 예약의_상태가_예약됨인지_확인() {
        //given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //when
        boolean result = reservation.isReserved();

        //then
        assertThat(result).isTrue();
    }
}
