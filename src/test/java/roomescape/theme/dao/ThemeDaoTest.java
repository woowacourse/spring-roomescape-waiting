package roomescape.theme.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

@JdbcTest
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeDaoTest {
    private static final int COUNT_OF_THEME = 3;
    private static final String EXISTED_NAME = "레벨2 탈출";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ThemeDao themeDao;

    @BeforeEach
    void setUp() {
        themeDao = new ThemeDao(jdbcTemplate);
    }

    @DisplayName("모든 테마를 조회할 수 있다.")
    @Test
    void findThemesTest() {
        List<Theme> expected = List.of(
                new Theme(1L, "레벨2 탈출", "우테코 레벨2 탈출기!", "https://img.jpg"),
                new Theme(2L, "레벨3 탈출", "우테코 레벨3 탈출기!", "https://img.jpg"),
                new Theme(3L, "레벨4 탈출", "우테코 레벨4 탈출기!", "https://img.jpg"));

        List<Theme> actual = themeDao.findThemes();

        assertThat(actual).containsAll(expected);
    }

    @DisplayName("id를 통해 테마를 조회할 수 있다.")
    @Test
    void findThemeByIdTest() {
        Optional<Theme> expected = Optional.of(
                new Theme(1L, "레벨2 탈출", "우테코 레벨2 탈출기!", "https://img.jpg"));

        Optional<Theme> actual = themeDao.findThemeById(1L);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("해당 id의 테마가 없을 경우, 빈 값을 반환한다.")
    @Test
    void findThemeByIdTest_whenThemeNotExist() {
        Long themeId = (long) COUNT_OF_THEME + 1;

        Optional<Theme> actual = themeDao.findThemeById(themeId);

        assertThat(actual).isEmpty();
    }

    @DisplayName("특정 기간 내 인기 테마를 예약 횟수 순으로 조회할 수 있다.")
    @Test
    void findThemesSortedByCountOfReservationTest() {
        List<Theme> expected = List.of(
                new Theme(1L, "레벨2 탈출", "우테코 레벨2 탈출기!", "https://img.jpg"));

        LocalDate start = LocalDate.of(2022, 5, 5);
        LocalDate end = LocalDate.of(2022, 5, 6);
        int count = 1;
        List<Theme> actual = themeDao.findThemesSortedByCountOfReservation(start, end, count);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("테마를 생성할 수 있다.")
    @Test
    void createThemeTest() {
        long expectedId = COUNT_OF_THEME + 1;
        Theme theme = new Theme("레벨1 탈출", "레벨1 탈출하기", "https://img.jpg");
        Theme expected = new Theme(expectedId, "레벨1 탈출", "레벨1 탈출하기", "https://img.jpg");

        Theme actual = themeDao.createTheme(theme);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("이미 해당 테마 이름이 존재한다면, 테마를 생성할 수 없다.")
    @Test
    void createThemeTest_whenNameIsAlreadyExist() {
        Theme theme = new Theme(EXISTED_NAME, "레벨2 탈출하기 대작전!", "https://img1.jpg");

        assertThatThrownBy(() -> themeDao.createTheme(theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 테마 이름은 이미 존재합니다.");
    }

    @DisplayName("테마를 삭제할 수 있다.")
    @Test
    void deleteThemeTest() {
        themeDao.deleteTheme(3L);

        assertThat(countSavedTheme()).isEqualTo(COUNT_OF_THEME - 1);
    }

    @DisplayName("해당 테마에 예약이 있다면, 예약 시간을 삭제할 수 없다.")
    @Test
    void deleteThemeTest_whenReservationUsingThemeExist() {
        assertThatThrownBy(() -> themeDao.deleteTheme(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 테마는 이미 예약되어 있어 삭제할 수 없습니다.");
    }

    private int countSavedTheme() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM theme", Integer.class);
    }
}
