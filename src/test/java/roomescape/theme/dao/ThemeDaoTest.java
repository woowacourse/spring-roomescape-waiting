package roomescape.theme.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.theme.Theme;
import roomescape.time.dao.TimeDao;

@DataJdbcTest
@Import(ThemeDao.class)
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
    void 테마_전체_조회_성공() {
        List<Theme> themes = themeDao.selectAll();
        assertThat(themes.size()).isEqualTo(11);
    }

    @Test
    void 테마_단일_조회_성공() {
        Theme firstTheme = themeDao.selectById(1L)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
        assertThat(firstTheme.getName()).isEqualTo("은하수");

        Theme secoundTheme = themeDao.selectById(2L)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
        assertThat(secoundTheme.getName()).isEqualTo("지구");
    }

    @Test
    void 테마_생성_성공() {
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
    void 테마_삭제_성공() {
        long id = 1L;
        themeDao.deleteById(id);

        List<Theme> themes = themeDao.selectAll();
        assertThat(themes.size()).isEqualTo(10);
    }
}
