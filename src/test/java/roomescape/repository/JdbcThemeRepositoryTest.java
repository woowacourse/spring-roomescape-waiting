package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@JdbcTest
class JdbcThemeRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ThemeRepository themeRepository;

    @Autowired
    public JdbcThemeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("테마를 추가한다.")
    void save() {
        Theme save = themeRepository.save(new Theme(null, "테마1", "테마1 설명", "https://example1.com"));

        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, "theme");
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(count).isEqualTo(1);
            softly.assertThat(save.getName()).isEqualTo("테마1");
            softly.assertThat(save.getDescription()).isEqualTo("테마1 설명");
            softly.assertThat(save.getThumbnail()).isEqualTo("https://example1.com");
        });
    }

    @Test
    @DisplayName("모든 테마들을 조회한다.")
    void findAll() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) "
                + "VALUES ('테마1', '테마 설명1', 'https://example1.com')");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeRepository.findAll()).hasSize(1);
            softly.assertThat(themeRepository.findAll().get(0).getName()).isEqualTo("테마1");
            softly.assertThat(themeRepository.findAll().get(0).getDescription()).isEqualTo("테마 설명1");
        });
    }

    @Test
    @DisplayName("테마를 조회한다.")
    void findById() {
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) "
                + "VALUES (1L, '테마1', '테마1 설명','https://example1.com')");

        Optional<Theme> foundTheme = themeRepository.findById(1L);
        assertThat(foundTheme).isPresent();
        assertThat(foundTheme.get().getName()).isEqualTo("테마1");
        assertThat(foundTheme.get().getDescription()).isEqualTo("테마1 설명");
        assertThat(foundTheme.get().getThumbnail()).isEqualTo("https://example1.com");
    }

    @Test
    @DisplayName("인기 있는 테마들을 조회한다.")
    void findPopularThemes() {
        String insertMemberSQL = """
                    INSERT INTO member (id, email, password, name, role)
                    VALUES (1, 'example1@gmail.com', 'password', 'name1', 'USER'),
                           (2, 'example2@gmail.com', 'password', 'name2', 'USER');
                """;

        String insertThemeSQL = """
                    INSERT INTO theme (id, name, description, thumbnail) 
                    VALUES (1, '테마1', '테마1 설명', 'https://example1.com'),
                           (2, '테마2', '테마2 설명', 'https://example2.com'),
                           (3, '테마3', '테마3 설명', 'https://example3.com');
                """;
        String insertTimeSQL = """
                    INSERT INTO reservation_time (id, start_at)
                    VALUES (1, '09:00'),
                           (2, '10:00'),
                           (3, '11:00');
                """;
        String insertReservationSQL = """
                    INSERT INTO reservation (id, date, member_id, time_id, theme_id)
                    VALUES (1, '2024-05-04', 1, 1, 1),
                           (2, '2024-05-05', 2, 2, 1),
                           (3, '2024-05-06', 1, 3, 1),
                           (4, '2024-05-07', 2, 1, 2),
                           (5, '2024-05-08', 1, 2, 2),
                           (6, '2024-05-09', 2, 3, 3),
                           (7, '2024-05-10', 1, 1, 3),
                           (8, '2024-05-11', 2, 2, 3),
                           (9, '2024-05-12', 1, 3, 3);
                """;

        jdbcTemplate.update(insertMemberSQL);
        jdbcTemplate.update(insertTimeSQL);
        jdbcTemplate.update(insertThemeSQL);
        jdbcTemplate.update(insertReservationSQL);

        LocalDate startDate = LocalDate.of(2024, 5, 6);
        LocalDate endDate = LocalDate.of(2024, 5, 11);
        int limit = 6;

        List<Theme> popularThemes = themeRepository.findPopularThemes(startDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);
            softly.assertThat(popularThemes.get(0).getId()).isEqualTo(3);
            softly.assertThat(popularThemes.get(1).getId()).isEqualTo(2);
            softly.assertThat(popularThemes.get(2).getId()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteById() {
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail)"
                + " VALUES (1, '테마1', '테마1 설명', 'https://example1.com')");

        themeRepository.deleteById(1L);

        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, "theme");
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("id에 해당하는 테마가 존재하는지 확인한다.")
    void existsById() {
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) "
                + "VALUES (1, '테마1', '테마1 설명', 'https://example1.com')");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeRepository.existsById(1L)).isTrue();
            softly.assertThat(themeRepository.existsById(2L)).isFalse();
        });
    }

    @Test
    @DisplayName("name에 해당하는 테마가 존재하는지 확인한다.")
    void existsByName() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) "
                + "VALUES ('테마1', '테마1 설명', 'https://example1.com')");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeRepository.existsByName("테마1")).isTrue();
            softly.assertThat(themeRepository.existsByName("테마2")).isFalse();
        });
    }
}
