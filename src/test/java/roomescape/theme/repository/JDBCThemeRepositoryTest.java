package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.reservation.fixture.TestFixture;
import roomescape.theme.domain.Theme;

class JDBCThemeRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScripts("/schema.sql", "/data.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(database);
        themeRepository = new JDBCThemeRepository(jdbcTemplate);

    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DROP TABLE reservation IF EXISTS");
        jdbcTemplate.update("DROP TABLE reservation_time IF EXISTS");
        jdbcTemplate.update("DROP TABLE theme IF EXISTS");
        jdbcTemplate.update("DROP TABLE member IF EXISTS");
    }

    @Test
    void findTop10PopularThemesWithinLastWeek_shouldReturnCorrectly() {
        List<Theme> top10PopularThemesWithinLastWeek = themeRepository.findTop10PopularThemesWithinLastWeek(
                TestFixture.makeNowDate());

        List<Long> ids = top10PopularThemesWithinLastWeek.stream()
                .map(Theme::getId)
                .toList();
        assertThat(ids).containsExactly(12L, 11L, 3L, 4L, 5L, 8L, 9L, 7L, 10L, 6L);
    }
}
