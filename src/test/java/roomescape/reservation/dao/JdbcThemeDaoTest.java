package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.model.Theme;
import roomescape.reservation.exception.ThemeNotExistException;
import roomescape.reservation.dao.JdbcThemeDao;

@JdbcTest
@Import(JdbcThemeDao.class)
class JdbcThemeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JdbcThemeDao jdbcThemeDao;

    @Test
    void DataSource_접근_테스트() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getTables(null, null, "THEME", null)
                    .next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("전체 테마를 조회할 수 있다.")
    void findAllThemes() {
        List<Theme> times = jdbcThemeDao.findAll();
        assertThat(times).hasSize(3);
    }

    @Test
    @DisplayName("테마를 추가할 수 있다.")
    void add() {
        Theme theme = new Theme(null, "이름", "설명", "썸네일");
        Theme newTheme = jdbcThemeDao.add(theme);

        assertThat(newTheme).isNotNull();
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void findAll() {
        // Given
        // When
        // Then
        assertThat(jdbcThemeDao.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("ID로 테마가 존재한다면 조회할 수 있다.")
    void findById() {
        Theme theme = new Theme(null, "이름", "설명", "썸네일");
        Theme actual = jdbcThemeDao.add(theme);

        Theme expected = jdbcThemeDao.findById(actual.getId());

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
            assertThat(actual.getThumbnail()).isEqualTo(expected.getThumbnail());
        });
    }

    @Test
    @DisplayName("ID로 테마가 존재하지 않는다면 예외가 발생한다.")
    void cannotFindById() {
        assertThatThrownBy(() -> jdbcThemeDao.findById(100L))
            .isInstanceOf(ThemeNotExistException.class)
            .hasMessage("테마를 찾을 수 없다.");
    }

    @Test
    @DisplayName("일주일 동안의 인기 테마를 검색할 수 있다.")
    void findMostReservedThemesInPeriodWithLimit() {
        LocalDate date = LocalDate.of(2025, 5, 6);
        List<Theme> themes = jdbcThemeDao.findMostReservedThemesInPeriodWithLimit(date.minusDays(7), date, 2);
        assertAll(() -> {
            assertThat(themes.getFirst().getId()).isEqualTo(2);
            assertThat(themes.getLast().getId()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("ID로 테마를 삭제할 수 있다.")
    void deleteById() {
        Theme theme = new Theme(null, "이름", "설명", "썸네일");
        Theme newTheme = jdbcThemeDao.add(theme);
        Long id = newTheme.getId();

        jdbcThemeDao.deleteById(id);

        assertThatThrownBy(() -> jdbcThemeDao.findById(id))
            .isInstanceOf(ThemeNotExistException.class);
    }

    @Test
    @DisplayName("테마가 존재하는지 확인할 수 있다.")
    void existByName() {
        String name = "이름";
        Theme theme = new Theme(null, name, "설명", "썸네일");
        jdbcThemeDao.add(theme);
        assertThat(jdbcThemeDao.existByName(name)).isTrue();
    }
}
