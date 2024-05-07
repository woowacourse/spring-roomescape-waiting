package roomescape.theme.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.theme.domain.Theme;

@JdbcTest
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeDaoTest {

    private final ThemeJdbcDao themeJdbcDao;

    @Autowired
    public ThemeDaoTest(DataSource dataSource) {
        this.themeJdbcDao = new ThemeJdbcDao(dataSource);
    }

    @Test
    @DisplayName("테마 정보가 DB에 정상적으로 저장되는지 확인한다.")
    void saveTheme() {
        Theme theme = new Theme("포레스트", "공포 테마", "thumbnail");
        Theme result = themeJdbcDao.save(theme);

        assertAll(() -> {
            assertEquals(result.getName(), theme.getName());
            assertEquals(result.getDescription(), theme.getDescription());
            assertEquals(result.getThumbnail(), theme.getThumbnail());
        });
    }

    @Test
    @DisplayName("테마 정보들을 정상적으로 가져오는지 확인한다.")
    void getThemes() {
        List<Theme> themes = themeJdbcDao.findAll();

        Assertions.assertThat(themes.size())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("테마 정보들이 정상적으로 제거되었는지 확인한다.")
    void deleteThemes() {
        themeJdbcDao.deleteById(3L);

        Assertions.assertThat(themeJdbcDao.findAll()
                        .size())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("지난 7일 기준 예약이 많은 테마 순으로 조회한다.")
    void getTopReservationThemes() {
        LocalDate reservationStartDate = LocalDate.now().minusDays(6);
        List<Theme> themes = themeJdbcDao.findByDateOrderByCount(reservationStartDate
                , reservationStartDate.plusWeeks(1));

        Assertions.assertThat(themes.get(0)
                        .getId())
                .isEqualTo(1);
    }

}
