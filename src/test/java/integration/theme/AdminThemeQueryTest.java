package integration.theme;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;
import roomescape.controller.admin.api.query.AdminThemeQuery;

class AdminThemeQueryTest extends BaseIntegrationTest {

    @Autowired
    private AdminThemeQuery adminThemeQuery;
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
    void 관리자_테마_목록은_활성화_여부를_포함해_전체_테마를_조회한다() {
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
        List<AdminThemeResponse> result = adminThemeQuery.getAllThemes();

        // then
        assertThat(result)
                .extracting(AdminThemeResponse::name)
                .containsExactly("활성", "비활성");
        assertThat(result)
                .extracting(AdminThemeResponse::isActive)
                .containsExactly(true, false);
    }
}
