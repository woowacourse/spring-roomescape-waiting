package roomescape.domain.reservationtime;

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

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("시간을 저장한다.")
    void save() {
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime saved = reservationTimeRepository.save(time);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("모든 시간을 조회한다.")
    void findAll() {
        int beforeSize = reservationTimeRepository.findAll().size();
        reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

        List<ReservationTime> times = reservationTimeRepository.findAll();

        assertThat(times).hasSize(beforeSize + 1);
    }

    @Test
    @DisplayName("ID로 시간을 삭제한다.")
    void deleteById() {
        ReservationTime saved = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        int beforeSize = reservationTimeRepository.findAll().size();

        int deletedCount = reservationTimeRepository.deleteById(saved.getId());

        assertThat(deletedCount).isEqualTo(1);
        assertThat(reservationTimeRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    @DisplayName("특정 시간이 존재하는지 확인한다.")
    void existsByStartAt() {
        LocalTime startAt = LocalTime.of(10, 0);
        reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));

        boolean exists = reservationTimeRepository.existsByStartAt(startAt);

        assertThat(exists).isTrue();
    }
}
