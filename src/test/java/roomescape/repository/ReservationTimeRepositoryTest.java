package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ReservationTimeRepository dao;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("DELETE FROM reservation_time;");
        this.dao = new ReservationTimeRepository(jdbcTemplate);
    }

    @Test
    void 시간_추가_테스트() {
        // given
        ReservationTime time = new ReservationTime(null, LocalTime.of(8, 0));

        // when
        ReservationTime result = dao.insert(time);

        // then
        List<ReservationTime> times = dao.findAll();
        ReservationTime savedTime = dao.findById(result.getId()).get();
        assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getStartAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(times).hasSize(1),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time.getStartAt()));
    }

    @Test
    void 예약_삭제_테스트() {
        // given
        ReservationTime time1 = new ReservationTime(null, LocalTime.of(8, 0));
        ReservationTime time2 = new ReservationTime(null, LocalTime.of(21, 0));
        ReservationTime savedTime1 = dao.insert(time1);
        ReservationTime savedTime2 = dao.insert(time2);

        // when
        int deletedCount = dao.delete(savedTime1.getId());

        // then
        List<ReservationTime> times = dao.findAll();
        assertAll(
                () -> assertThat(deletedCount).isEqualTo(1),
                () -> assertThat(times).hasSize(1),
                () -> assertThat(dao.findById(savedTime1.getId())).isEmpty(),
                () -> assertThat(dao.findById(savedTime2.getId())).isPresent());
    }
}
