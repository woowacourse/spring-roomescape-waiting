package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.domain.Theme;

@JdbcTest
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("테마를 저장한다.")
    @Test
    void save() {
        // when
        Theme saved = jdbcThemeRepository.save(new Theme("테마", "내용", "https://img.test/b.png"));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테마");
        assertThat(saved.getDescription()).isEqualTo("내용");
        assertThat(saved.getImageUrl()).isEqualTo("https://img.test/b.png");
    }

    @DisplayName("저장된 테마를 조회한다.")
    @Test
    void findAll() {
        // given
        Theme saved = jdbcThemeRepository.save(new Theme("이름", "설명", "https://img.test/a.png"));

        // when
        List<Theme> themes = jdbcThemeRepository.findAll();

        // then
        assertThat(themes).hasSize(1);
        assertThat(themes.getFirst().getId()).isEqualTo(saved.getId());
        assertThat(themes.getFirst().getName()).isEqualTo("이름");
        assertThat(themes.getFirst().getDescription()).isEqualTo("설명");
        assertThat(themes.getFirst().getImageUrl()).isEqualTo("https://img.test/a.png");
    }

    @DisplayName("id로 테마를 삭제한다.")
    @Test
    void deleteById() {
        // given
        Theme saved = jdbcThemeRepository.save(new Theme("x", "y", "https://img.test/c.png"));

        // when & then
        assertThat(jdbcThemeRepository.deleteById(saved.getId())).isTrue();
        assertThat(jdbcThemeRepository.findAll()).isEmpty();
        assertThat(jdbcThemeRepository.deleteById(saved.getId())).isFalse();
    }

    @DisplayName("id로 테마가 존재하는지 판단한다.")
    @Test
    void existsById() {
        // given
        Theme saved = jdbcThemeRepository.save(new Theme("테마", "설명", "https://img.test/a.png"));

        // when & then
        assertThat(jdbcThemeRepository.existsById(saved.getId())).isTrue();
        assertThat(jdbcThemeRepository.existsById(saved.getId() + 1)).isFalse();
    }

    @DisplayName("특정 날짜의 인기 테마를 조회한다.")
    @Test
    void findBestThemesByDate_인기_테마_조회() {
        // given
        Theme theme1 = jdbcThemeRepository.save(new Theme("테마1", "설명1", "https://img.test/1.png"));
        Theme theme2 = jdbcThemeRepository.save(new Theme("테마2", "설명2", "https://img.test/2.png"));
        Theme theme3 = jdbcThemeRepository.save(new Theme("테마3", "설명3", "https://img.test/3.png"));

        LocalDate startDate = LocalDate.of(2026, 5, 3);
        LocalDate endDate = LocalDate.of(2026, 5, 10);

        Long timeId7 = insertTime("2026-05-07 10:00:00", "2026-05-07 12:00:00");
        Long timeId8 = insertTime("2026-05-08 10:00:00", "2026-05-08 12:00:00");
        Long timeId9 = insertTime("2026-05-09 10:00:00", "2026-05-09 12:00:00");
        Long timeId10 = insertTime("2026-05-10 10:00:00", "2026-05-10 12:00:00");
        Long timeId6 = insertTime("2026-05-06 10:00:00", "2026-05-06 12:00:00");
        Long timeId2 = insertTime("2026-05-02 10:00:00", "2026-05-02 12:00:00");

        insertReservation("a1", timeId7, theme1.getId());
        insertReservation("a2", timeId8, theme1.getId());
        insertReservation("a3", timeId10, theme1.getId());

        insertReservation("b1", timeId9, theme2.getId());
        insertReservation("b2", timeId10, theme2.getId());
        insertReservation("b_in", timeId6, theme2.getId());

        insertReservation("c_out", timeId2, theme3.getId());

        // when
        List<Theme> bestThemes = jdbcThemeRepository.findBestThemesByDate(startDate, endDate, 10);

        // then
        assertThat(bestThemes).hasSize(2);
        assertThat(bestThemes.get(0).getId()).isEqualTo(theme1.getId());
        assertThat(bestThemes.get(1).getId()).isEqualTo(theme2.getId());
        assertThat(bestThemes).extracting(Theme::getName)
                .containsExactly("테마1", "테마2");
    }

    private Long insertTime(String startAt, String endAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_time, end_time) VALUES (?, ?)",
                startAt,
                endAt
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_time = ?",
                Long.class,
                startAt
        );
    }

    private void insertReservation(String name, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, time_id, theme_id, status, created_at) VALUES (?, ?, ?, ?, ?)",
                name,
                timeId,
                themeId,
                "RESERVED",
                LocalDateTime.now()
        );
    }
}
