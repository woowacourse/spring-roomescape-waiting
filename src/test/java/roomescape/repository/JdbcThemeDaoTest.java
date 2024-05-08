package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.model.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcThemeDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeDao themeDao;

    private SimpleJdbcInsert themeInsertActor;
    private SimpleJdbcInsert reservationInsertActor;
    private SimpleJdbcInsert timeInsertActor;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        themeInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        reservationInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        timeInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        insertTheme("에버", "공포", "공포.jpg");
        insertTheme("배키", "미스터리", "미스터리.jpg");
        List<Theme> allThemes = themeDao.findAllThemes();
        assertThat(allThemes).hasSize(2);
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void should_add_theme() {
        insertTheme("에버", "공포", "공포.jpg");
        insertTheme("배키", "미스터리", "미스터리.jpg");
        Theme theme = new Theme("브라운", "공포", "공포.jpg");
        themeDao.addTheme(theme);
        assertThat(themeDao.findAllThemes()).hasSize(3);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        insertTheme("에버", "공포", "공포.jpg");
        insertTheme("배키", "미스터리", "미스터리.jpg");
        themeDao.deleteTheme(1);
        assertThat(themeDao.findAllThemes()).hasSize(1);
    }

    @DisplayName("특정 기간의 테마를 인기순으로 정렬하여 조회한다.")
    @Test
    void should_find_ranking_theme_by_date() {
        insertReservationTime(LocalTime.of(10, 0));
        for (int i = 1; i <= 15; i++) {
            insertTheme("name" + i, "description" + i, "thumbnail" + i);
        }
        for (int i = 1; i <= 10; i++) {
            insertReservation("name" + i, LocalDate.of(2030, 1, i % 7 + 1), 1L, i);
        }
        insertReservation("name11", LocalDate.of(2030, 1, 1), 1L, 10);
        insertReservation("name12", LocalDate.of(2030, 1, 2), 1L, 10);
        insertReservation("name13", LocalDate.of(2030, 1, 3), 1L, 10);
        insertReservation("name14", LocalDate.of(2030, 1, 4), 1L, 9);
        insertReservation("name15", LocalDate.of(2030, 1, 5), 1L, 9);

        LocalDate before = LocalDate.of(2030, 1, 1);
        LocalDate after = LocalDate.of(2030, 1, 7);
        List<Theme> themes = themeDao.findThemeRankingByDate(before, after, 10);

        assertSoftly(softly -> {
            softly.assertThat(themes).hasSize(10);
            softly.assertThat(themes).containsExactly(
                    new Theme(10L, "name10", "description10", "thumbnail10"),
                    new Theme(9L, "name9", "description9", "thumbnail9"),
                    new Theme(1L, "name1", "description1", "thumbnail1"),
                    new Theme(2L, "name2", "description2", "thumbnail2"),
                    new Theme(3L, "name3", "description3", "thumbnail3"),
                    new Theme(4L, "name4", "description4", "thumbnail4"),
                    new Theme(5L, "name5", "description5", "thumbnail5"),
                    new Theme(6L, "name6", "description6", "thumbnail6"),
                    new Theme(7L, "name7", "description7", "thumbnail7"),
                    new Theme(8L, "name8", "description8", "thumbnail8")
            );
        });
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertReservationTime(LocalTime startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertReservation(String name, LocalDate date, long timeId, long themeId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        reservationInsertActor.execute(parameters);
    }
}
