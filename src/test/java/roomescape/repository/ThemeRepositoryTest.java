package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ThemeDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ThemeRepositoryTest {

    private static final int INITIAL_THEME_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final ThemeRepository themeRepository;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;

    @Autowired
    public ThemeRepositoryTest(JdbcTemplate jdbcTemplate, ThemeRepository themeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeRepository = themeRepository;
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.reservationInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertTheme("n1", "d1", "t1");
        insertTheme("n2", "d2", "t2");
        insertReservationTime("1:00");
        insertReservationTime("2:00");
        insertReservation("2000-01-01", 1, 1, 1);
        insertReservation("2000-01-02", 2, 2, 2);
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertReservation(String date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        List<Theme> themes = themeRepository.findAllThemes();
        assertThat(themes).hasSize(2);
    }

    @DisplayName("테마를 저장한 후 저장된 테마를 반환한다.")
    @Test
    void should_save_theme_and_return() {
        ThemeDto themeDto = new ThemeDto("n3", "d3", "t3");
        Theme before = Theme.from(themeDto);

        Optional<Theme> actual = themeRepository.saveTheme(before);

        Theme after = new Theme(3, "n3", "d3", "t3");
        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(after);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme_by_id() {
        themeRepository.deleteThemeById(1);
        assertThat(themeRepository.findAllThemes()).hasSize(INITIAL_THEME_COUNT - 1);
    }

    @DisplayName("두 날짜 사이의 예약을 테마의 개수로 내림차순 정렬하여, 특정 개수의 테마를 조회한다.")
    @Test
    void should_find_theme_ranking_by_date() {
        List<Theme> themes = themeRepository.findThemeRankingByDate(
                LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2), 10);
        assertThat(themes).hasSizeLessThanOrEqualTo(10);
        assertThat(themes).containsExactly(
                new Theme(1, "n1", "d1", "t1"),
                new Theme(2, "n2", "d2", "t2")
        );
    }
}