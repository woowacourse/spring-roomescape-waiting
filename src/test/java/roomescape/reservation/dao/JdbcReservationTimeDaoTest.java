package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.exception.TimeNotExistException;
import roomescape.reservation.dao.JdbcReservationTimeDao;

@JdbcTest
@Import(JdbcReservationTimeDao.class)
public class JdbcReservationTimeDaoTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcReservationTimeDao jdbcReservationTimeDao;

    @Test
    @DisplayName("시간을 추가할 수 있다.")
    void add() {
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        ReservationTime newReservationTime = jdbcReservationTimeDao.add(reservationTime);

        assertThat(newReservationTime).isNotNull();
    }

    @Test
    @DisplayName("전체 시간을 조회할 수 있다.")
    void findAll() {
        List<ReservationTime> times = jdbcReservationTimeDao.findAll();

        assertThat(times).hasSize(5);
    }

    @Test
    @DisplayName("ID로 시간이 존재한다면 조회할 수 있다.")
    void findById() {
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        ReservationTime actual = jdbcReservationTimeDao.add(reservationTime);

        ReservationTime expected = jdbcReservationTimeDao.findById(actual.getId());

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getStartAt()).isEqualTo(expected.getStartAt());
        });
    }

    @Test
    @DisplayName("ID로 시간이 존재하지 않는다면 예외가 발생한다.")
    void cannotFindById() {
        assertThatThrownBy(() -> jdbcReservationTimeDao.findById(100L))
            .isInstanceOf(TimeNotExistException.class)
            .hasMessage("예약 시간을 찾을 수 없다.");
    }

    @Test
    @DisplayName("ID로 시간을 삭제할 수 있다.")
    void deleteById() {
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        ReservationTime newReservationTime = jdbcReservationTimeDao.add(reservationTime);
        Long id = newReservationTime.getId();

        jdbcReservationTimeDao.deleteById(id);

        assertThatThrownBy(() -> jdbcReservationTimeDao.findById(id))
            .isInstanceOf(TimeNotExistException.class);
    }

    @Test
    @DisplayName("시간이 존재하는지 확인할 수 있다.")
    void existByStartAt() {
        LocalTime startAt = LocalTime.of(13, 0);
        ReservationTime reservationTime = new ReservationTime(null, startAt);
        jdbcReservationTimeDao.add(reservationTime);
        assertThat(jdbcReservationTimeDao.existByStartAt(startAt)).isTrue();
    }
}
