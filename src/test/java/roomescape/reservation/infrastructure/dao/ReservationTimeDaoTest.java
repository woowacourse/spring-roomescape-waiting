package roomescape.reservation.infrastructure.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;

@JdbcTest
public class ReservationTimeDaoTest {

    private final ReservationTimeDao reservationTimeDao;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationTimeDaoTest(JdbcTemplate jdbcTemplate) {
        this.reservationTimeDao = new ReservationTimeDao(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @DisplayName("시간 추가 확인 테스트")
    void insertTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));

        // when
        reservationTimeDao.insert(reservationTimeRequest.getStartAt());

        // then
        assertThat(count()).isEqualTo(1);
    }

    @Test
    @DisplayName("시간 전체 조회 테스트")
    void findAllTimesTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        reservationTimeDao.insert(reservationTimeRequest.getStartAt());

        // when
        assertThat(reservationTimeDao.findAllTimes()).hasSize(1);
    }

    @Test
    @DisplayName("시간 삭제 확인 테스트")
    void deleteTest() {
        // when
        reservationTimeDao.delete(0L);

        // then
        assertThat(count()).isEqualTo(0);
    }

    private int count() {
        String sql = "select count(*) from reservation_time";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

}
