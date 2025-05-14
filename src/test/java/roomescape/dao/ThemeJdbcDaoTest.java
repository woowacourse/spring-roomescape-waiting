package roomescape.dao;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.model.Theme;

@JdbcTest
@Import(ThemeJdbcDao.class)
public class ThemeJdbcDaoTest {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (resultSet, rowNum) -> (
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail"))
    );

    @Autowired
    private ThemeJdbcDao themeJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long savedId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "공포 테마", "무서워요",
                "image");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "마법사 테마", "마법사를 만나요",
                "image");
        this.savedId = saveNewTheme(new Theme("새로운 테마", "새 테마에요", "image"));
    }

    @Test
    @DisplayName("모든 테마 이름을 조회한다")
    void test1() {
        List<Theme> themes = themeJdbcDao.findAll();
        assertThat(themes.stream()
                .map(Theme::getName).toList())
                .contains("공포 테마", "마법사 테마", "새로운 테마");
    }

    @Test
    @DisplayName("테마를 저장한다")
    void test2() {
        // given
        Theme theme = new Theme("저장 테스트용 테마", "저장을 테스트해요", "image");

        // when
        Long savedId = themeJdbcDao.saveTheme(theme);

        // then
        List<Theme> foundThemes = jdbcTemplate.query(
                "SELECT * FROM theme where id = ?",
                THEME_ROW_MAPPER,
                savedId
        );

        assertAll(
                () -> assertThat(foundThemes).hasSize(1),
                () -> assertThat(foundThemes.getFirst().getName()).isEqualTo("저장 테스트용 테마"),
                () -> assertThat(foundThemes.getFirst().getDescription()).isEqualTo("저장을 테스트해요"),
                () -> assertThat(foundThemes.getFirst().getThumbnail()).isEqualTo("image")
        );

    }

    @Test
    @DisplayName("id 를 이용해 테마를 찾는다")
    void test3() {
        // when
        Optional<Theme> foundTheme = themeJdbcDao.findById(savedId);

        // then
        assertAll(
                () -> assertThat(foundTheme.isPresent()).isTrue(),
                () -> assertThat(foundTheme.get().getName()).isEqualTo("새로운 테마"),
                () -> assertThat(foundTheme.get().getDescription()).isEqualTo("새 테마에요"),
                () -> assertThat(foundTheme.get().getThumbnail()).isEqualTo("image")
        );
    }

    @Test
    @DisplayName("id 를 이용해 테마를 삭제한다")
    void test4() {
        // when
        themeJdbcDao.deleteById(savedId);

        // then
        List<Theme> foundThemes = jdbcTemplate.query(
                "SELECT * FROM theme where id = ?",
                THEME_ROW_MAPPER,
                savedId
        );

        assertThat(foundThemes).hasSize(0);
    }

    @Test
    @DisplayName("중복되는 이름의 테마가 존재하는지 확인한다")
    void test5() {
        // when
        boolean result = themeJdbcDao.isDuplicatedNameExisted("공포 테마");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("조회 시점의 이전 날짜를 기준으로 특정 범위만큼의 인기 테마를 예약이 많은 건수대로 조회한다")
    void test6() {
        // given
        LocalDate date = LocalDate.of(2024, 10, 31);
        int dayRange = 3;

        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id) VALUES (?, ?, ?)",
                date.minusDays(1), 1, savedId
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id) VALUES (?, ?, ?)",
                date.minusDays(2), 1, savedId
        );

        Long newSavedId = saveNewTheme(new Theme("인기 테마", "인기 있는 테마", "image"));
        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id) VALUES (?, ?, ?)",
                date.minusDays(2), 1, newSavedId
        );

        // when
        List<Theme> popularThemes = themeJdbcDao.getTopReservedThemesSince(date, dayRange, 10);

        // then
        assertAll(
                () -> assertThat(popularThemes).hasSize(2),
                () -> assertThat(popularThemes.getFirst().getName()).isEqualTo("새로운 테마"),
                () -> assertThat(popularThemes.get(1).getName()).isEqualTo("인기 테마")
        );
    }

    private Long saveNewTheme(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
