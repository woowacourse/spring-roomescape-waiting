package roomescape.domain.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;
import roomescape.testFixture.JdbcHelper;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcHelper.truncateAll(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 수 기준으로 테마 순위를 조회한다")
    void findThemeRankingTest() {
        // given
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) VALUES (?, ?, ?, ?)", 1L, "테마1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) VALUES (?, ?, ?, ?)", 2L, "테마2", "설명2", "썸네일2");
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) VALUES (?, ?, ?, ?)", 3L, "테마3", "설명3", "썸네일3");

        jdbcTemplate.update("INSERT INTO member (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)", 1L, "user", "user@example.com", "password", "USER");

        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)", 1L, LocalTime.of(14, 0));

        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update("INSERT INTO waiting (id, saved_date_time, status) VALUES (?, ?, ?)", 1L, date, "RESERVED");
        jdbcTemplate.update("INSERT INTO waiting (id, saved_date_time, status) VALUES (?, ?, ?)", 2L, date, "RESERVED");
        jdbcTemplate.update("INSERT INTO waiting (id, saved_date_time, status) VALUES (?, ?, ?)", 3L, date, "RESERVED");
        jdbcTemplate.update("INSERT INTO waiting (id, saved_date_time, status) VALUES (?, ?, ?)", 4L, date, "RESERVED");

        jdbcTemplate.update("INSERT INTO reservation (member_id, theme_id, date, time_id, waiting_id) VALUES (?, ?, ?, ?, ?)",
                1L, 1L, date.toLocalDate(), 1L, 1L);
        jdbcTemplate.update("INSERT INTO reservation (member_id, theme_id, date, time_id, waiting_id) VALUES (?, ?, ?, ?, ?)",
                1L, 1L, date.toLocalDate(), 1L, 2L);
        jdbcTemplate.update("INSERT INTO reservation (member_id, theme_id, date, time_id, waiting_id) VALUES (?, ?, ?, ?, ?)",
                1L, 1L, date.toLocalDate(), 1L, 3L);
        jdbcTemplate.update("INSERT INTO reservation (member_id, theme_id, date, time_id, waiting_id) VALUES (?, ?, ?, ?, ?)",
                1L, 2L, date.toLocalDate(), 1L, 4L);

        // when
        List<Theme> result = themeRepository.findThemeRanking(
                date.toLocalDate().minusDays(1),
                date.toLocalDate().plusDays(1),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(3L);
    }
}
