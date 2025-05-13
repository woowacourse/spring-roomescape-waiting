package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.reservation.domain.ReservationTime;

@JdbcTest
@Sql(scripts = "/schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationTimeDaoTest {

    private ReservationTimeDao reservationTimeDao;
    private final LocalTime time = LocalTime.of(10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        reservationTimeDao = new JdbcReservationTimeDao(namedParameterJdbcTemplate);
    }

    @DisplayName("새로운 예약시간을 저장할 수 있다.")
    @Test
    void save() {
        // given
        ReservationTime newReservation = new ReservationTime(time);
        // when
        ReservationTime result = reservationTimeDao.save(newReservation);
        // then
        ReservationTime savedReservationTime = reservationTimeDao.findAll().getFirst();
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getStartAt()).isEqualTo(time),
                () -> assertThat(savedReservationTime.getId()).isEqualTo(1L),
                () -> assertThat(savedReservationTime.getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("예약시간 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        reservationTimeDao.save(new ReservationTime(time));
        reservationTimeDao.save(new ReservationTime(time.plusHours(1)));
        // when
        List<ReservationTime> result = reservationTimeDao.findAll();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("id를 기반으로 예약 시간을 조회할 수 있다")
    @Test
    void findById() {
        // given
        ReservationTime savedReservationTime = reservationTimeDao.save(new ReservationTime(time));
        // when
        Optional<ReservationTime> result = reservationTimeDao.findById(savedReservationTime.getId());
        // then
        assertAll(
                () -> assertThat(result).isNotEmpty(),
                () -> assertThat(result.get().getId()).isEqualTo(savedReservationTime.getId()),
                () -> assertThat(result.get().getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("시간을 기반으로 예약시간의 존재 여부를 반환할 수 있다")
    @Test
    void isExistsByTime() {
        // given
        reservationTimeDao.save(new ReservationTime(time));
        // when
        boolean result = reservationTimeDao.isExistsByTime(time);
        // then
        assertThat(result).isTrue();
    }

    @DisplayName("id를 기반으로 예약시간이 존재하지 않는지 여부를 반환할 수 있다")
    @Test
    void deleteById() {
        // given
        ReservationTime savedReservationTime = reservationTimeDao.save(new ReservationTime(time));
        // when
        reservationTimeDao.deleteById(savedReservationTime.getId());
        // then
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();
        assertThat(reservationTimes).isEmpty();
    }
}
