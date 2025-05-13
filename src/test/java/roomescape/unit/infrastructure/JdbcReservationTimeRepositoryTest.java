package roomescape.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.infrastructure.JdbcReservationTimeRepository;

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public JdbcReservationTimeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Test
    void 예약_시간을_추가할_수_있다() {

        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));

        // when
        reservationTimeRepository.save(reservationTime);
        List<ReservationTime> reservationTimeDaoAll = reservationTimeRepository.findAll();

        // then
        assertThat(reservationTimeDaoAll.size()).isEqualTo(1);
    }

    @Test
    void 예약_시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));

        // when
        reservationTimeRepository.save(reservationTime);
        List<ReservationTime> reservationTimeDaoAll = reservationTimeRepository.findAll();

        // then
        assertThat(reservationTimeDaoAll.getFirst().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        int beforeSize = reservationTimeRepository.findAll().size();

        // when
        reservationTimeRepository.deleteById(savedTime.getId());
        int afterSize = reservationTimeRepository.findAll().size();

        // then
        assertThat(beforeSize).isEqualTo(1);
        assertThat(afterSize).isEqualTo(0);
    }
}
