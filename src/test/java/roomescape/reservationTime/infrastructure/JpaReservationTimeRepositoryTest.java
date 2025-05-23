package roomescape.reservationTime.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @Autowired
    JpaReservationTimeRepository jpaReservationTimeRepository;

    @Test
    void 모든_예약_시간을_조회_할_수_있다() {
        List<ReservationTime> times = jpaReservationTimeRepository.findAll();

        assertThat(times)
            .extracting(ReservationTime::getStartAt)
            .containsExactly(
                LocalTime.of(10,0),
                LocalTime.of(11,0),
                LocalTime.of(12,0));
    }
}