package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.ReservationTime;

@JdbcTest
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcReservationTimeRepository jdbcReservationTimeRepository;

    @Test
    void 예약_시간을_추가한다() {
        // given
        final LocalTime startAt = LocalTime.parse("10:00");
        final ReservationTime reservationTime = new ReservationTime(null, startAt);

        // when & then
        Assertions.assertThatCode(() -> jdbcReservationTimeRepository.save(reservationTime))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_정보_목록을_조회한다() {
        // given
        final LocalTime time1 = LocalTime.parse("10:00");
        final ReservationTime reservationTime1 = new ReservationTime(null, time1);

        final LocalTime time2 = LocalTime.parse("11:00");
        final ReservationTime reservationTime2 = new ReservationTime(null, time2);
        jdbcReservationTimeRepository.save(reservationTime1);
        jdbcReservationTimeRepository.save(reservationTime2);

        // when
        final List<ReservationTime> reservationTimes = jdbcReservationTimeRepository.findAll();

        // then
        assertThat(reservationTimes.size()).isEqualTo(2);
    }
}
