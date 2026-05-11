package roomescape.theme.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.theme.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeDaoTest {
    private static final RowMapper<Theme> rowMapper =
            (rs, rowNum) ->
                    new Theme(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("image"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 데이터베이스_연동() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "THEME", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 테마_전체_조회_테스트() {
        List<Theme> themes = themeDao.selectAll();
        assertThat(themes.size()).isEqualTo(11);
    }

    @Test
    void 테마_단일_조회_테스트() {
        Theme firstTheme = themeDao.selectById(1L);
        assertThat(firstTheme.getName()).isEqualTo("은하수");

        Theme secoundTheme = themeDao.selectById(2L);
        assertThat(secoundTheme.getName()).isEqualTo("지구");
    }

    @Test
    void 테마_생성_테스트() {
        Theme theme = new Theme("디스커버리", "디스커버리 테마방입니다", "http.jp");
        Theme expected = themeDao.insert(theme);

        String sql = "SELECT * FROM theme WHERE id = ?";
        Theme actual = jdbcTemplate.query(sql, rowMapper, expected.getId()).getFirst();

        assertThat(expected.getId()).isEqualTo(actual.getId());
        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.getDescription()).isEqualTo(actual.getDescription());
        assertThat(expected.getImage()).isEqualTo(actual.getImage());
    }

    @Test
    void 테마_삭제_테스트() {
        long id = 1L;
        themeDao.deleteById(id);

        List<Theme> themes = themeDao.selectAll();
        assertThat(themes.size()).isEqualTo(10);
    }
}
