package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.infrastructure.repository.ThemeJdbcRepository;

@JdbcTest
@Import(ThemeJdbcRepository.class)
class ThemeJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeJdbcRepository repository;

    private Long timeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);
    }

    @Test
    void save는_생성된_id를_부여한_테마를_반환한다() {
        Theme theme = new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg");

        Theme saved = repository.save(theme);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("공포");
    }

    @Test
    void 예약에서_사용_중인_테마를_삭제하면_ConflictException을_던진다() {
        Theme saved = repository.save(new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg"));
        insertReservation("브라운", LocalDate.of(2026, 8, 5), saved.getId());

        assertThatThrownBy(() -> repository.deleteById(saved.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("사용 중인 예약");
    }

    @Test
    void findPopularThemes는_윈도우_내_예약수가_많은_테마부터_반환한다() {
        Long themeA = repository.save(new Theme(null, "공포", "A", "url")).getId();
        Long themeB = repository.save(new Theme(null, "추리", "B", "url")).getId();

        // 윈도우: 2026-04-29 ~ 2026-05-05
        insertReservation("u1", LocalDate.of(2026, 5, 5), themeA);
        insertReservation("u2", LocalDate.of(2026, 5, 4), themeA); // A: 2건
        insertReservation("u3", LocalDate.of(2026, 5, 3), themeB); // B: 1건

        List<Theme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                10
        );

        assertThat(popular)
                .extracting(Theme::getName)
                .containsExactly("공포", "추리");
    }

    @Test
    void findPopularThemes는_윈도우_밖_예약을_세지_않는다() {
        Long themeId = repository.save(new Theme(null, "공포", "A", "url")).getId();

        // 윈도우: 2026-04-29 ~ 2026-05-05
        insertReservation("u1", LocalDate.of(2026, 4, 28), themeId); // 윈도우 직전
        insertReservation("u2", LocalDate.of(2026, 5, 6), themeId);  // 윈도우 직후

        List<Theme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                10
        );

        assertThat(popular).isEmpty();
    }

    @Test
    void findPopularThemes는_limit으로_결과_개수를_제한한다() {
        Long themeA = repository.save(new Theme(null, "A", "", "url")).getId();
        Long themeB = repository.save(new Theme(null, "B", "", "url")).getId();
        Long themeC = repository.save(new Theme(null, "C", "", "url")).getId();

        insertReservation("u1", LocalDate.of(2026, 5, 5), themeA);
        insertReservation("u2", LocalDate.of(2026, 5, 4), themeB);
        insertReservation("u3", LocalDate.of(2026, 5, 3), themeC);

        List<Theme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                2
        );

        assertThat(popular).hasSize(2);
    }

    private void insertReservation(String name, LocalDate date, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId
        );
    }
}
