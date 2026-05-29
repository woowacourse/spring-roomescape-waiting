package integration.theme;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.client.api.dto.response.ThemeResponse;
import roomescape.controller.client.api.query.ThemeQuery;

class ThemeQueryTest extends BaseIntegrationTest {

    @Autowired
    private ThemeQuery themeQuery;
    @Autowired
    private ThemeDataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
    }

    @Test
    void 활성화된_테마만_조회한다() {
        // given
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_image_url, is_active)
                VALUES (?, ?, ?, ?)
                """, "활성", "활성 테마", "https://image.com/active.png", true);
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_image_url, is_active)
                VALUES (?, ?, ?, ?)
                """, "비활성", "비활성 테마", "https://image.com/inactive.png", false);

        // when
        List<ThemeResponse> result = themeQuery.getAllActiveThemes();

        // then
        assertThat(result)
                .extracting(ThemeResponse::name)
                .containsExactly("활성");
    }

    @Test
    void 기간_내_예약이_많은_활성_테마순으로_인기_테마를_조회한다() {
        // given
        dataSource.insertThemesByCount(3);
        dataSource.insertTimeByStartToEndWithOneHourRotation(10, 12);
        dataSource.insertReservationByTheme(1L, 1);
        dataSource.insertReservationByTheme(2L, 3);
        jdbcTemplate.update("UPDATE theme SET is_active = 0 WHERE id = ?", 3L);

        // when
        List<ThemeResponse> result = themeQuery.getPopularThemes(LocalDate.now().minusDays(1), LocalDate.now());

        // then
        assertThat(result)
                .extracting(ThemeResponse::name)
                .containsExactly("테마1", "테마0");
    }
}
