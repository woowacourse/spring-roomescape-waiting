package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

@JdbcTest
@Sql(scripts = "/schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeDaoTest {
    private ThemeDao themeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        themeDao = new JdbcThemeDao(namedParameterJdbcTemplate);
    }

    @DisplayName("새로운 테마를 저장할 수 있다")
    @Test
    void save() {
        // given
        String name = "leo";
        String description = "hihihi";
        String thumbnail = "hihihihi";
        Theme theme = new Theme(null, name, description, thumbnail);
        // when
        Theme result = themeDao.save(theme);
        // then
        Theme savedTheme = themeDao.findById(result.getId()).get();
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getDescription()).isEqualTo(description),
                () -> assertThat(result.getThumbnail()).isEqualTo(thumbnail),
                () -> assertThat(savedTheme.getId()).isEqualTo(1L),
                () -> assertThat(savedTheme.getName()).isEqualTo(name),
                () -> assertThat(savedTheme.getDescription()).isEqualTo(description),
                () -> assertThat(savedTheme.getThumbnail()).isEqualTo(thumbnail)
        );
    }

    @DisplayName("이름을 기반으로 테마 존재 여부를 반환할 수 있다")
    @Test
    void isExists() {
        // given
        String name = "테마1";
        String description = "hihihi";
        String thumbnail = "hihihihi";
        themeDao.save(new Theme(null, name, description, thumbnail));
        // when
        boolean result = themeDao.isExists(new ThemeName(name));
        // then
        assertThat(result).isTrue();
    }

    @DisplayName("테마 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        themeDao.save(new Theme(null, "테마1", "우테코탈출", "하고싶지않아"));
        themeDao.save(new Theme(null, "테마2", "2단계탈출", "하고싶어요"));
        // when
        List<Theme> result = themeDao.findAll();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("id를 기반으로 테마를 조회할 수 있다")
    @Test
    void findById() {
        // given
        Theme savedTheme = themeDao.save(new Theme(null, "테마1", "우테코탈출", "하고싶지않아"));
        // when
        Optional<Theme> result = themeDao.findById(savedTheme.getId());
        // then
        assertAll(
                () -> assertThat(result).isNotEmpty(),
                () -> assertThat(result.get().getId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(result.get().getName()).isEqualTo(savedTheme.getName()),
                () -> assertThat(result.get().getDescription()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(result.get().getThumbnail()).isEqualTo(savedTheme.getThumbnail())
        );
    }

    @DisplayName("id를 기반으로 테마를 삭제할 수 있다")
    @Test
    void deleteById() {
        // given
        Theme savedTheme = themeDao.save(new Theme(null, "테마1", "우테코탈출", "하고싶지않아"));
        // when
        themeDao.deleteById(savedTheme.getId());
        // then
        List<Theme> themes = themeDao.findAll();
        assertThat(themes).isEmpty();
    }
}
