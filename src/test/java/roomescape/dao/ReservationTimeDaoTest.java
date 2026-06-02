package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.ReservationTime;

@JdbcTest
@Import(ReservationTimeDao.class)
class ReservationTimeDaoTest {

    private static final int DEFAULT_TIME_COUNT = 9;
    private static final Long AVAILABLE_TIME_ID = 1L;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    void 전체_시간_조회() {
        List<ReservationTime> times = reservationTimeDao.findAll();

        assertThat(times).hasSize(DEFAULT_TIME_COUNT);
    }

    @Test
    void ID로_시간_조회() {
        ReservationTime time = reservationTimeDao.findTimeById(AVAILABLE_TIME_ID);

        assertThat(time).isNotNull();
        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
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
        boolean exists = reservationTimeDao.existsByTimeId(AVAILABLE_TIME_ID);

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

        assertThatThrownBy(() -> reservationTimeDao.findTimeById(9L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
