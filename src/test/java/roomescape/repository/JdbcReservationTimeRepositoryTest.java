package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;

@JdbcTest
@Import(JdbcReservationTimeRepository.class)
@Sql(scripts = "/schema.sql")
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("시간을 저장하면 id가 채번되어 반환된다")
    void save() {
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("모든 시간을 조회한다")
    void findAll() {
        insertTime(LocalTime.of(10, 0));
        insertTime(LocalTime.of(11, 0));

        List<ReservationTime> times = reservationTimeRepository.findAll();

        assertThat(times).hasSize(2);
    }

    @Test
    @DisplayName("id로 시간을 조회한다")
    void findById() {
        long id = insertTime(LocalTime.of(10, 0));

        assertThat(reservationTimeRepository.findById(id)).isPresent();
        assertThat(reservationTimeRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("id로 시간을 삭제한다")
    void deleteById() {
        long id = insertTime(LocalTime.of(10, 0));

        reservationTimeRepository.deleteById(id);

        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("id 존재 여부를 확인한다")
    void existsById() {
        long id = insertTime(LocalTime.of(10, 0));

        assertThat(reservationTimeRepository.existsById(id)).isTrue();
        assertThat(reservationTimeRepository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("startAt 존재 여부를 확인한다")
    void existsByStartAt() {
        insertTime(LocalTime.of(10, 0));

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }

    private long insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt.toString());
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

}
