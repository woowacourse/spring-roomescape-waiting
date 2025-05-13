package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixtures.RESERVATION_TIME_1;
import static roomescape.TestFixtures.RESERVATION_TIME_2;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.infrastructure.persistance.JdbcReservationTimeRepository;

class JdbcReservationTimeRepositoryTest extends JdbcSupportTest {

    private final JdbcReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE), TEST_DATASOURCE);

    @Test
    void 예약_시간을_저장할_수_있다() {
        //when
        Long createdId = reservationTimeRepository.create(RESERVATION_TIME_1);

        //then
        assertThat(reservationTimeRepository.findById(createdId)).hasValue(RESERVATION_TIME_1);
    }

    @Test
    void id로_예약_시간을_조회할_수_있다() {
        //given
        insertReservationTime(RESERVATION_TIME_1);

        //when
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(1L);

        //then
        assertThat(reservationTime).hasValue(RESERVATION_TIME_1);
    }

    @Test
    void id값이_없다면_빈_Optional_값이_반환된다() {
        //when
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(1L);

        //then
        assertThat(reservationTime).isEmpty();
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        //given
        insertReservationTime(RESERVATION_TIME_1);
        insertReservationTime(RESERVATION_TIME_2);

        //when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        //then
        assertThat(reservationTimes).isEqualTo(List.of(
                RESERVATION_TIME_1,
                RESERVATION_TIME_2
        ));
    }

    @Test
    void id값으로_예약_시간을_삭제한다() {
        //given
        insertReservationTime(RESERVATION_TIME_1);

        //when
        reservationTimeRepository.deleteById(1L);

        //then
        assertThat(reservationTimeRepository.findById(1L)).isEmpty();
    }
}