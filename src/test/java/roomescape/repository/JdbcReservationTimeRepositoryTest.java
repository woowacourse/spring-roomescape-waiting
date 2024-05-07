package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationTimeRepository reservationTimeRepository;

    private ReservationTime savedReservationTime;

    @Autowired
    private JdbcReservationTimeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @BeforeEach
    void saveReservationTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        savedReservationTime = reservationTimeRepository.save(reservationTime);
    }

    @DisplayName("DB 시간 추가 테스트")
    @Test
    void save() {
        Optional<ReservationTime> findReservation = reservationTimeRepository.findById(savedReservationTime.getId());
        assertThat(findReservation).isPresent();
    }

    @DisplayName("DB 모든 시간 조회 테스트")
    @Test
    void findAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllReservationTimes();

        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("DB 시간 삭제 테스트")
    @Test
    void delete() {
        reservationTimeRepository.delete(savedReservationTime.getId());
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllReservationTimes();

        assertThat(reservationTimes).isEmpty();
    }
}
