package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.exception.ThemeInUseException;

@JdbcTest
@ActiveProfiles("test")
@Import({ThemeService.class, ThemeDao.class})
public class ThemeServiceIntegrationTest {

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_DEFAULT_MEMBER_SQL = """
            INSERT INTO member (id, email, password, name, role)
            VALUES (1, 'brown@email.com', 'password', '브라운', 'USER');
            """;

    private static final String INSERT_DEFAULT_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '테마', '설명', 'https://example.com/img.jpg');
            """;

    private static final String INSERT_DEFAULT_TIME_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00');
            """;

    private static final String INSERT_DEFAULT_RESERVATION_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1);
            """;

    private final ThemeService themeService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ThemeServiceIntegrationTest(ThemeService themeService, JdbcTemplate jdbcTemplate) {
        this.themeService = themeService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void 존재하지_않는_테마_삭제는_멱등하게_성공한다() {
        // given: 비어 있는 DB

        // when & then
        assertThatCode(() -> themeService.deleteTheme(999L))
                .doesNotThrowAnyException();
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_DEFAULT_MEMBER_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_DEFAULT_RESERVATION_SQL
    })
    void 예약이_있는_테마는_삭제할_수_없다() {
        // given: theme(1) 을 reservation(1) 이 참조 중

        // when & then: 삭제 시도 → FK 위반 예외
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(ThemeInUseException.class);

        // then: theme row 는 그대로 유지
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM theme WHERE id = 1", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
