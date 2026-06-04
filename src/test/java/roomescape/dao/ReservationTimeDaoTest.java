package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.ReservationTime;

@JdbcTest
@Import(ReservationTimeDao.class)
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    void ID로_시간_조회() {
        Optional<ReservationTime> time = reservationTimeDao.findTimeById(1L);

        assertThat(time)
                .map(ReservationTime::getId)
                .hasValue(1L);

        assertThat(time)
                .map(ReservationTime::getStartAt)
                .hasValue(LocalTime.of(10, 0));
    }

    @Test
    void 시간_저장() {
        ReservationTime newTime = new ReservationTime(LocalTime.of(19, 0));

        ReservationTime saved = reservationTimeDao.save(newTime);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void 예약에_사용중인_시간_존재하는_경우() {
        boolean exists = reservationTimeDao.existsByTimeId(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void 예약에_사용중인_시간_존재하지_않는_경우() {
        boolean exists = reservationTimeDao.existsByTimeId(9L);

        assertThat(exists).isFalse();
    }

    @Test
    void 시간_삭제() {
        reservationTimeDao.delete(9L);

        assertThat(reservationTimeDao.findTimeById(9L)).isEmpty();
    }
}
