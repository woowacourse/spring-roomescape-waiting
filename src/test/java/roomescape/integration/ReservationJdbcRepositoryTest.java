package roomescape.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.repository.ReservationJdbcRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(ReservationJdbcRepository.class)
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ReservationJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationJdbcRepository repository;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);
    }

    @Test
    void save는_생성된_id를_부여한_예약을_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        );

        Reservation saved = repository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.count()).isEqualTo(1L);
    }

    @Test
    void findByDateAndThemeId는_같은_날짜와_테마의_예약만_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        LocalDate targetDate = LocalDate.of(2026, 8, 5);
        repository.save(new Reservation(
                "브라운",
                targetDate,
                time,
                theme
        ));
        repository.save(new Reservation(
                "티뉴",
                LocalDate.of(2026, 8, 6),
                time,
                theme
        ));

        Reservations result = repository.findByDateAndThemeId(targetDate, themeId);

        assertThat(result.isOccupied(time)).isTrue();
    }

    @Test
    void existsByTimeId는_예약이_없으면_false를_반환한다() {
        assertThat(repository.existsByTimeId(timeId)).isFalse();
    }

    @Test
    void existsByTimeId는_예약이_있으면_true를_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        repository.save(new Reservation(
                "브라운",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        ));

        assertThat(repository.existsByTimeId(timeId)).isTrue();
    }

    @Test
    void deleteById_이후_findById는_빈_Optional을_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation saved = repository.save(new Reservation(
                "브라운",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        ));

        repository.deleteById(saved.getId());

        Optional<Reservation> found = repository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void findByName은_이름이_일치하는_예약만_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        repository.save(new Reservation(
                "민욱",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        ));
        repository.save(new Reservation(
                "티뉴",
                LocalDate.of(2026, 8, 6),
                time,
                theme
        ));

        assertThat(repository.findByName("민욱", 0, 10))
                .hasSize(1)
                .first()
                .extracting(Reservation::getName)
                .isEqualTo("민욱");
    }

    @Test
    void findByName은_offset과_limit으로_페이지를_잘라_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        repository.save(new Reservation(
                "민욱",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        ));
        repository.save(new Reservation(
                "민욱",
                LocalDate.of(2026, 8, 6),
                time,
                theme
        ));
        repository.save(new Reservation(
                "민욱",
                LocalDate.of(2026, 8, 7),
                time,
                theme
        ));

        assertThat(repository.findByName("민욱", 0, 2)).hasSize(2);
        assertThat(repository.findByName("민욱", 2, 2)).hasSize(1);
    }

    @Test
    void countByName은_이름이_일치하는_예약_수를_반환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        repository.save(new Reservation(
                "민욱",
                LocalDate.of(2026, 8, 5),
                time,
                theme
        ));
        repository.save(new Reservation(
                "민욱",
                LocalDate.of(2026, 8, 6),
                time,
                theme
        ));
        repository.save(new Reservation(
                "티뉴",
                LocalDate.of(2026, 8, 7),
                time,
                theme
        ));

        assertThat(repository.countByName("민욱")).isEqualTo(2L);
    }

    @Test
    void transferWithPendingStatus는_새_주인에게_예약을_넘기고_PENDING으로_전환한다() {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation saved = repository.save(new Reservation(
                null,
                "브라운",
                LocalDate.of(2026, 8, 5),
                time,
                theme,
                ReservationStatus.CONFIRM
        ));

        repository.transferWithPendingStatus(saved.getId(), "민욱");

        Optional<Reservation> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("민욱");
        assertThat(found.get().getReservationStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
