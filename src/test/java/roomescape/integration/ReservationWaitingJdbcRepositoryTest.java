package roomescape.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.repository.ReservationWaitingJdbcRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(ReservationWaitingJdbcRepository.class)
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ReservationWaitingJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationWaitingJdbcRepository repository;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "티뉴", LocalDate.of(2026, 8, 5), timeId, themeId
        );
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "공포", "무서운 테마", "https://example.com/horror.jpg");
        reservation = new Reservation(reservationId, "티뉴", LocalDate.of(2026, 8, 5), time, theme);
    }

    @Test
    void save는_생성된_id와_첫_순번을_부여해_반환한다() {
        ReservationWaiting waiting = new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation);

        ReservationWaiting saved = repository.save(waiting);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrder()).isEqualTo(1);
    }

    @Test
    void 같은_예약에_먼저_신청한_대기가_있으면_다음_순번을_부여한다() {
        repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        ReservationWaiting second = repository.save(new ReservationWaiting(
                "브라운", LocalDateTime.of(2026, 8, 1, 10, 0, 1), reservation));

        assertThat(second.getOrder()).isEqualTo(2);
    }

    @Test
    void existBy는_같은_이름과_예약의_대기가_있으면_true를_반환한다() {
        repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        assertThat(repository.existBy("민욱", reservation.getId())).isTrue();
    }

    @Test
    void existBy는_일치하는_대기가_없으면_false를_반환한다() {
        assertThat(repository.existBy("민욱", reservation.getId())).isFalse();
    }

    @Test
    void findById는_저장된_대기를_반환한다() {
        ReservationWaiting saved = repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        Optional<ReservationWaiting> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("민욱");
        assertThat(found.get().getReservation().getId()).isEqualTo(reservation.getId());
    }

    @Test
    void findById는_존재하지_않으면_빈_Optional을_반환한다() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void deleteById_이후_findById는_빈_Optional을_반환한다() {
        ReservationWaiting saved = repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
