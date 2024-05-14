package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.testutil.JdbcRepositoryTest;

@JdbcRepositoryTest
class JdbcReservationTimeRepositoryTest {

    private final JdbcReservationTimeRepository jdbcReservationTimeRepository;

    @Autowired
    JdbcReservationTimeRepositoryTest(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate, dataSource);
    }

    @Test
    @DisplayName("ReservationTime 저장한 후 저장한 row의 id값을 반환한다.")
    void save() {
        ReservationTime newReservationTime = new ReservationTime(null, LocalTime.of(10, 00));
        assertThat(jdbcReservationTimeRepository.save(newReservationTime)).isEqualTo(
                new ReservationTime(1L, LocalTime.of(10, 00)));
    }

    @Test
    @DisplayName("ReservationTime 테이블의 있는 모든 데이터를 조회한다.")
    void findAll() {
        List<ReservationTime> savedTimes = ReservationTimeFixture.get(2).stream()
                .map(jdbcReservationTimeRepository::save)
                .toList();
        assertThat(jdbcReservationTimeRepository.findAll()).isEqualTo(savedTimes);
    }

    @Test
    @DisplayName("ReservationTime 테이블의 주어진 id와 동일한 데이터를 조회한다.")
    void findById() {
        ReservationTime savedTime = jdbcReservationTimeRepository.save(ReservationTimeFixture.getOne());

        assertThat(jdbcReservationTimeRepository.findById(savedTime.getId())).isEqualTo(Optional.of(savedTime));
    }

    @Test
    @DisplayName("해당 예약 시간 id값과 일치하는 예약이 존재하는 경우 참을 반환한다.")
    void existsById() {
        ReservationTime savedTime = jdbcReservationTimeRepository.save(ReservationTimeFixture.getOne());
        assertTrue(jdbcReservationTimeRepository.existsById(savedTime.getId()));
    }

    @Test
    @DisplayName("해당 예약 시간 id값과 일치하는 예약이 존재하지 않은 경우 거짓을 반환한다.")
    void existsById_WhenNotExist() {
        assertFalse(jdbcReservationTimeRepository.existsById(7L));
    }

    @Test
    @DisplayName("해당 예약 시간 id값과 일치하는 예약이 존재하는 경우 참을 반환한다.")
    void existsByStartAt() {
        ReservationTime savedTime = jdbcReservationTimeRepository.save(ReservationTimeFixture.getOne());
        assertTrue(jdbcReservationTimeRepository.existsByStartAt(savedTime.getTime()));
    }

    @Test
    @DisplayName("해당 예약 시간 id값과 일치하는 예약이 존재하지 않은 경우 거짓을 반환한다.")
    void existsByStartAt_WhenNotExist() {
        assertFalse(jdbcReservationTimeRepository.existsByStartAt(LocalTime.parse("00:01")));
    }

    @Test
    @DisplayName("ReservationTime 테이블에 주어진 id와 없는 경우 빈 옵셔널을 반환한다.")
    void findById_Return_EmptyOptional() {
        assertThat(jdbcReservationTimeRepository.findById(10L)).isNotPresent();
    }

    @Test
    @DisplayName("ReservationTime 테이블에 주어진 id와 동일한 데이터를 삭제한다.")
    void deleteById() {
        jdbcReservationTimeRepository.deleteById(7L);
        assertThat(jdbcReservationTimeRepository.findById(7L)).isNotPresent();
    }
}
