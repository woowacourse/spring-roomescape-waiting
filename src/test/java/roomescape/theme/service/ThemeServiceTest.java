package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.theme.dto.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마B', '설명B', 'https://b.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마C', '설명C', 'https://c.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마D', '설명D', 'https://d.com')");

        // 인기 테마 데이터 (theme1 > theme2 > theme3)
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u1', ?, 1, 1)", LocalDate.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u2', ?, 1, 1)", LocalDate.now().minusDays(2));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u3', ?, 1, 1)", LocalDate.now().minusDays(3));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u1', ?, 2, 2)", LocalDate.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u2', ?, 2, 2)", LocalDate.now().minusDays(2));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u1', ?, 3, 3)", LocalDate.now().minusDays(1));
    }

    @Test
    @DisplayName("인기 테마 조회")
    void 인기_테마_조회() {
        List<ThemeResponse> result = themeService.getTopThemes(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("테마A");
        assertThat(result.get(1).name()).isEqualTo("테마B");
        assertThat(result.get(2).name()).isEqualTo("테마C");
    }

    @Test
    @DisplayName("인기 테마 조회 limit 적용")
    void 인기_테마_조회_limit_적용() {
        List<ThemeResponse> result = themeService.getTopThemes(2);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("id로 테마 조회 성공")
    void getById_성공() {
        assertThat(themeService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id로 테마 조회 시 예외 발생")
    void getById_없으면_예외() {
        assertThatThrownBy(() -> themeService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND))
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());
    }
}
