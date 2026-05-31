package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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

    @Test
    @DisplayName("예약된 시간도 포함해 모든 가용 시간을 반환한다")
    void findAvailable() {
        long bookedTimeId = insertTime(LocalTime.of(10, 0));
        long freeTimeId = insertTime(LocalTime.of(11, 0));
        long themeId = insertTheme("무인도 탈출");
        LocalDate date = LocalDate.of(2099, 12, 31);
        insertReservation("브라운", date, bookedTimeId, themeId);

        List<ReservationTime> available = reservationTimeRepository.findAvailable(date, themeId);

        assertThat(available).extracting(ReservationTime::getId)
                .containsExactlyInAnyOrder(bookedTimeId, freeTimeId);
    }

    private long insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt.toString());
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

    private long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, "설명", "https://example.com/thumb.jpg"
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
    }

    private void insertReservation(String name, LocalDate date, long timeId, long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (reserver_name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date.toString(), timeId, themeId
        );
    }
}
