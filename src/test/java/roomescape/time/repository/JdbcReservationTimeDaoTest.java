package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.time.domain.ReservationTime;

@JdbcTest
@Import(JdbcReservationTimeDao.class)
@Sql({"/test-schema.sql", "/test-time-data.sql"})
class JdbcReservationTimeDaoTest {

    @Autowired
    private JdbcReservationTimeDao jdbcReservationTimeDao;

    @DisplayName("예약 시간을 저장할 수 있다.")
    @Test
    void testSave() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        // when
        ReservationTime savedTime = jdbcReservationTimeDao.save(time);
        // then
        assertAll(
                () -> assertThat(savedTime.getId()).isEqualTo(4L),
                () -> assertThat(jdbcReservationTimeDao.findById(4L).orElseThrow().getStartAt())
                        .isEqualTo(LocalTime.of(10, 0))
        );
    }

    @DisplayName("모든 예약 시간을 불러올 수 있다.")
    @Test
    void testFindAll() {
        // when
        List<ReservationTime> actual = jdbcReservationTimeDao.findAll();
        // then
        assertThat(actual).hasSize(3);
    }

    @DisplayName("ID로 특정 예약 시간을 불러올 수 있다.")
    @Test
    void testFindById() {
        // when
        ReservationTime actual = jdbcReservationTimeDao.findById(3L).orElseThrow();
        // then
        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(3L),
                () -> assertThat(actual.getStartAt()).isEqualTo(LocalTime.of(12, 0))
        );
    }

    @DisplayName("ID로 특정 예약 시간을 삭제할 수 있다.")
    @Test
    void testDeleteById() {
        // when
        jdbcReservationTimeDao.deleteById(2L);
        // then
        List<ReservationTime> actual = jdbcReservationTimeDao.findAll();
        Condition<ReservationTime> idIs2L = new Condition<>(time -> time.getId() == 2L, "Id is 2L");
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual).doNotHave(idIs2L)
        );
    }

    @DisplayName("특정 시간 값이 존재하는지 확인할 수 있다.")
    @Test
    void testExistByTime() {
        // when
        boolean actual = jdbcReservationTimeDao.existsByTime(LocalTime.of(10, 0));
        // then
        assertThat(actual).isTrue();
    }
}
