package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ReservationTimeRepository dao;

    @BeforeEach
    void setup() {
        this.dao = new ReservationTimeRepository(jdbcTemplate);
    }

    @Test
    void 시간_추가_테스트() {
        // given
        ReservationTime time = new ReservationTime(null, LocalTime.of(8, 0));

        // when
        Long id = dao.insert(time);

        // then
        ReservationTime savedTime = dao.findBy(id).get();
        assertAll(
                () -> assertThat(id).isNotNull(),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time.getStartAt()));
    }

    @Test
    void 예약_삭제_테스트() {
        // given
        ReservationTime time1 = new ReservationTime(null, LocalTime.of(8, 0));
        ReservationTime time2 = new ReservationTime(null, LocalTime.of(21, 0));
        Long id1 = dao.insert(time1);
        Long id2 = dao.insert(time2);

        // when
        int deletedCount = dao.delete(id1);

        // then
        assertAll(
                () -> assertThat(deletedCount).isEqualTo(1),
                () -> assertThat(dao.findBy(id1)).isEmpty(),
                () -> assertThat(dao.findBy(id2)).isPresent());
    }
}
