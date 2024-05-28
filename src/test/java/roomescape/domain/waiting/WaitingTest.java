package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.ReservationRepository;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    void 확정된_예약으로_대기번호_생성할_경우_예외() {
        //given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //when, then
        assertThatThrownBy(() -> new Waiting(reservation, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
