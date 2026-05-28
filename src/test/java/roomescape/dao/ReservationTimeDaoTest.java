package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeDao;

@JdbcTest
@Import(ReservationTimeDao.class)
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @DisplayName("시간을 저장하면 생성된 ID를 반환한다.")
    @Test
    void 시간_저장() {
        Long id = reservationTimeDao.save(LocalTime.of(9, 0));

        assertThat(id).isNotNull().isPositive();
    }

    @DisplayName("ID로 시간을 조회하면 해당 시간 정보가 반환된다.")
    @Test
    void ID로_시간_조회() {
        // data.sql: ID 1 = 10:00
        ReservationTime time = reservationTimeDao.findById(1L);

        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("전체 시간 목록을 조회하면 초기 데이터 13건이 반환된다.")
    @Test
    void 전체_시간_조회() {
        List<ReservationTime> times = reservationTimeDao.findAll();

        assertThat(times).hasSize(13);
    }

    @DisplayName("시간을 삭제하면 목록에서 제거된다.")
    @Test
    void 시간_삭제() {
        Long id = reservationTimeDao.save(LocalTime.of(9, 0));

        reservationTimeDao.delete(id);

        assertThat(reservationTimeDao.findAll()).hasSize(13);
    }
}
