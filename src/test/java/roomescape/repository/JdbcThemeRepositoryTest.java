package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcThemeRepository jdbcThemeRepository;

    @BeforeEach
    void setUp() {
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("테마를 저장하고 영속화된 객체를 반환한다.")
    void 테마_저장() {
        Theme theme = new Theme("공포", "귀신의 집", "https://url");
        Theme savedTheme = jdbcThemeRepository.save(theme);
        assertThat(savedTheme.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 테마를 조회한다.")
    void 식별자로_테마_조회() {
        Theme savedTheme = jdbcThemeRepository.save(new Theme("공포", "귀신의 집", "https://url"));
        Optional<Theme> foundTheme = jdbcThemeRepository.findById(savedTheme.getId());
        assertThat(foundTheme).isPresent();
        assertThat(foundTheme.get().getName()).isEqualTo("공포");
    }

    @Test
    @DisplayName("모든 테마 목록을 조회한다.")
    void 전체_테마_조회() {
        jdbcThemeRepository.save(new Theme("공포", "귀신의 집", "https://url"));
        List<Theme> themes = jdbcThemeRepository.findAll();
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("기간 내 인기 테마를 예약 건수 기반으로 조회한다.")
    void 인기_테마_조회() {
        Theme savedTheme = jdbcThemeRepository.save(new Theme("공포", "귀신의 집", "https://url"));
        insertReservation(savedTheme.getId());
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = startDate.plusDays(2);
        List<Theme> themes = jdbcThemeRepository.findPopularThemes(10L, startDate, endDate);
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("존재하는 테마를 삭제한다.")
    void 존재하는_테마_삭제() {
        Theme savedTheme = jdbcThemeRepository.save(new Theme("공포", "귀신의 집", "https://url"));
        int totalCount = jdbcThemeRepository.findAll().size();
        jdbcThemeRepository.deleteById(savedTheme.getId());
        assertThat(jdbcThemeRepository.findAll().size() != totalCount).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제해도 예외가 발생하지 않는다.")
    void 존재하지_않는_테마_삭제() {
        assertThatCode(() -> jdbcThemeRepository.deleteById(999L))
                .doesNotThrowAnyException();
    }

    private void insertReservation(long themeId) {
        jdbcTemplate.update("INSERT INTO time_slot (start_at) VALUES (?)", "10:00:00");
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM time_slot WHERE start_at = ?", Long.class,
                "10:00:00");
        jdbcTemplate.update("INSERT INTO reservation_slot (date, time_id, theme_id) VALUES (?, ?, ?)",
                LocalDate.now(), timeId, themeId);
        Long slotId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                LocalDate.now(),
                timeId,
                themeId
        );
        String sql = "INSERT INTO reservation (name, slot_id, created_at, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "test", slotId, LocalDate.now().atStartOfDay(), "RESERVED");
    }
}
